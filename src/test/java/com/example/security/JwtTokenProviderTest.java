package com.example.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void generateAndValidateToken() {
        // Generate token
        String userId = "testuser";
        String token = jwtTokenProvider.generateToken(userId);

        // Validate token
        assertTrue(jwtTokenProvider.validateToken(token));
        
        // Verify userId extraction
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void invalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void malformedToken() {
        String malformedToken = "malformedtoken";
        assertFalse(jwtTokenProvider.validateToken(malformedToken));
    }

    @Test
    void nullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void getUserIdFromInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertThrows(JwtException.class, () -> 
            jwtTokenProvider.getUserIdFromToken(invalidToken)
        );
    }

    @Test
    void multipleTokenGeneration() {
        String userId = "testuser";
        String token1 = jwtTokenProvider.generateToken(userId);
        String token2 = jwtTokenProvider.generateToken(userId);

        // Verify both tokens are valid but different
        assertNotEquals(token1, token2);
        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token1));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token2));
    }
}
