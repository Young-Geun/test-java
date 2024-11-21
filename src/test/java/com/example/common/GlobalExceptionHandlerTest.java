package com.example.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleValidationExceptions() throws Exception {
        String invalidSignupRequest = """
            {
                "userId": "",
                "password": "pwd",
                "email": "invalid-email"
            }
            """;

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidSignupRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.errors.userId").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void handleBadCredentialsException() throws Exception {
        String invalidLoginRequest = """
            {
                "userId": "nonexistent",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLoginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void handleApiException() throws Exception {
        // This will trigger ApiException for duplicate user
        String signupRequest = """
            {
                "userId": "testuser",
                "password": "password123",
                "email": "test@example.com"
            }
            """;

        // First signup
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupRequest))
                .andExpect(status().isOk());

        // Second signup with same userId
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User ID already exists"));
    }
}
