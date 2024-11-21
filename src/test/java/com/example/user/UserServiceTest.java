package com.example.user;

import com.example.common.ApiException;
import com.example.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSignupSuccess() {
        UserDto.SignupRequest request = new UserDto.SignupRequest(
            "testuser",
            "password123",
            "test@example.com"
        );

        assertDoesNotThrow(() -> userService.signup(request));

        User savedUser = userRepository.findByUserId("testuser").orElseThrow();
        assertEquals("testuser", savedUser.getUserId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertTrue(savedUser.isEnabled());
        assertFalse(savedUser.isAccountLocked());
    }

    @Test
    void testSignupDuplicateUserId() {
        UserDto.SignupRequest request = new UserDto.SignupRequest(
            "testuser",
            "password123",
            "test@example.com"
        );

        userService.signup(request);

        UserDto.SignupRequest duplicateRequest = new UserDto.SignupRequest(
            "testuser",
            "password456",
            "other@example.com"
        );

        ApiException exception = assertThrows(ApiException.class,
            () -> userService.signup(duplicateRequest));
        assertEquals("User ID already exists", exception.getMessage());
    }

    @Test
    void testSignupDuplicateEmail() {
        UserDto.SignupRequest request = new UserDto.SignupRequest(
            "user1",
            "password123",
            "test@example.com"
        );

        userService.signup(request);

        UserDto.SignupRequest duplicateRequest = new UserDto.SignupRequest(
            "user2",
            "password456",
            "test@example.com"
        );

        ApiException exception = assertThrows(ApiException.class,
            () -> userService.signup(duplicateRequest));
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testLoginSuccess() {
        // Create user
        UserDto.SignupRequest signupRequest = new UserDto.SignupRequest(
            "testuser",
            "password123",
            "test@example.com"
        );
        userService.signup(signupRequest);

        // Test login
        UserDto.LoginRequest loginRequest = new UserDto.LoginRequest(
            "testuser",
            "password123"
        );

        Map<String, String> result = userService.login(loginRequest);
        assertNotNull(result.get("token"));
        assertTrue(jwtTokenProvider.validateToken(result.get("token")));
    }

    @Test
    void testLoginFailureAndAccountLock() {
        // Create user
        UserDto.SignupRequest signupRequest = new UserDto.SignupRequest(
            "testuser",
            "password123",
            "test@example.com"
        );
        userService.signup(signupRequest);

        UserDto.LoginRequest wrongPasswordRequest = new UserDto.LoginRequest(
            "testuser",
            "wrongpassword"
        );

        // Test 5 failed attempts
        for (int i = 0; i < 4; i++) {
            ApiException exception = assertThrows(ApiException.class,
                () -> userService.login(wrongPasswordRequest));
            assertEquals("Invalid credentials", exception.getMessage());
        }

        // 5th attempt should lock the account
        ApiException exception = assertThrows(ApiException.class,
            () -> userService.login(wrongPasswordRequest));
        assertTrue(exception.getMessage().contains("Account has been locked"));

        // Verify account is locked
        User user = userRepository.findByUserId("testuser").orElseThrow();
        assertTrue(user.isAccountLocked());
    }

    @Test
    void testGetAllUsers() {
        // Create multiple users
        UserDto.SignupRequest request1 = new UserDto.SignupRequest(
            "user1",
            "password123",
            "user1@example.com"
        );
        UserDto.SignupRequest request2 = new UserDto.SignupRequest(
            "user2",
            "password123",
            "user2@example.com"
        );

        userService.signup(request1);
        userService.signup(request2);

        List<UserDto.Response> users = userService.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUserId().equals("user1")));
        assertTrue(users.stream().anyMatch(u -> u.getUserId().equals("user2")));
    }
}
