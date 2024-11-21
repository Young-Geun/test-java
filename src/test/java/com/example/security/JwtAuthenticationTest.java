package com.example.security;

import com.example.user.User;
import com.example.user.UserDto;
import com.example.user.UserRepository;
import com.example.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create test user
        User user = new User();
        user.setUserId("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmail("test@example.com");
        userRepository.save(user);
    }

    @Test
    void testJwtTokenFlow() throws Exception {
        // Test 1: Generate JWT token
        UserDto.LoginRequest loginRequest = new UserDto.LoginRequest("testuser", "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String firstToken = extractToken(loginResult);
        assertNotNull(firstToken);

        // Test 2: Access API with valid token
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk());

        // Test 3: Generate new token for same user
        MvcResult newLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String secondToken = extractToken(newLoginResult);
        assertNotNull(secondToken);
        assertNotEquals(firstToken, secondToken);

        // Test 4: Try to access API with old token (should still work as we haven't implemented token blacklisting)
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk());

        // Test 5: Access API with new token
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidToken() throws Exception {
        // Test invalid token format
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMissingToken() throws Exception {
        // Test missing token
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    private String extractToken(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        Map<String, String> response = objectMapper.readValue(content, Map.class);
        return response.get("token");
    }
}
