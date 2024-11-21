package com.example.security;

import com.example.user.User;
import com.example.user.UserDto;
import com.example.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TokenValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USER_ID = "testuser";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        createTestUser();
    }

    @Test
    void testInvalidTokenFormat() throws Exception {
        // Test with malformed token
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid.token.format"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token format"));

        // Test with non-JWT token
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + "randomstring"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMalformedAuthorizationHeader() throws Exception {
        // Test with missing "Bearer" prefix
        mockMvc.perform(get("/api/users")
                .header("Authorization", getValidToken()))
                .andExpect(status().isUnauthorized());

        // Test with wrong prefix
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Basic " + getValidToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testTokenAfterUserLocked() throws Exception {
        // Get valid token
        String token = getValidToken();

        // Lock the user
        User user = userRepository.findByUserId(TEST_USER_ID).orElseThrow();
        user.lock();
        userRepository.save(user);

        // Try to use token after user is locked
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testTokenAfterUserDisabled() throws Exception {
        // Get valid token
        String token = getValidToken();

        // Disable the user
        User user = userRepository.findByUserId(TEST_USER_ID).orElseThrow();
        user.setEnabled(false);
        userRepository.save(user);

        // Try to use token after user is disabled
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private void createTestUser() {
        User user = new User();
        user.setUserId(TEST_USER_ID);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setEmail("test@example.com");
        userRepository.save(user);
    }

    private String getValidToken() throws Exception {
        UserDto.LoginRequest loginRequest = new UserDto.LoginRequest(TEST_USER_ID, TEST_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Map<String, String> response = objectMapper.readValue(content, Map.class);
        return response.get("token");
    }
}
