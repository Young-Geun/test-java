package com.example.user;

import com.example.common.ApiException;
import com.example.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(UserDto.SignupRequest request) {
        validateSignupRequest(request);

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        
        userRepository.save(user);
    }

    private void validateSignupRequest(UserDto.SignupRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw ApiException.badRequest("User ID already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.badRequest("Email already exists");
        }
    }

    public Map<String, String> login(UserDto.LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> ApiException.unauthorized("Invalid credentials"));

        if (!user.isEnabled()) {
            throw ApiException.unauthorized("Account is disabled");
        }

        if (user.isAccountLocked()) {
            if (user.getLockTime().plusHours(24).isAfter(LocalDateTime.now())) {
                throw ApiException.unauthorized("Account is locked. Please try again after 24 hours");
            }
            user.unlock();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.increaseFailedAttempts();
            
            if (user.getFailedAttempt() >= 5) {
                user.lock();
                userRepository.save(user);
                throw ApiException.unauthorized("Account has been locked due to 5 failed attempts. Please try again after 24 hours");
            }
            
            userRepository.save(user);
            throw ApiException.unauthorized("Invalid credentials");
        }

        user.resetFailedAttempts();
        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getUserId());
        return Map.of("token", token);
    }

    public List<UserDto.Response> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto.Response(
                        user.getUserId(),
                        user.getEmail(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()))
                .toList();
    }
}
