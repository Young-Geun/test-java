package com.example.security;

import com.example.user.User;
import com.example.user.UserDto;
import com.example.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TokenRefreshTest {

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
    void testTokenRefreshScenario() throws Exception {
        // First login and get token
        String firstToken = loginAndGetToken(TEST_USER_ID, TEST_PASSWORD);
        assertNotNull(firstToken);

        // Verify first token works
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk());

        // Wait a short time to ensure tokens are different
        TimeUnit.MILLISECONDS.sleep(100);

        // Login again and get new token
        String secondToken = loginAndGetToken(TEST_USER_ID, TEST_PASSWORD);
        assertNotNull(secondToken);
        assertNotEquals(firstToken, secondToken);

        // Verify new token works
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk());

        // Verify old token still works (as we haven't implemented token blacklisting)
        // In a real production environment, you might want to implement token blacklisting
        // and this test would expect status().isUnauthorized()
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk());
    }

    @Test
    void testConcurrentTokenUsage() throws Exception {
        // Get two tokens in quick succession
        String firstToken = loginAndGetToken(TEST_USER_ID, TEST_PASSWORD);
        String secondToken = loginAndGetToken(TEST_USER_ID, TEST_PASSWORD);

        // Both tokens should be valid and usable
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk());
    }

    private void createTestUser() {
        User user = new User();
        user.setUserId(TEST_USER_ID);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setEmail("test@example.com");
        userRepository.save(user);
    }

    private String loginAndGetToken(String userId, String password) throws Exception {
        UserDto.LoginRequest loginRequest = new UserDto.LoginRequest(userId, password);
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
