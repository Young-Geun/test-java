package com.example.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class UserDto {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class SignupRequest {
        @NotBlank(message = "User ID is required")
        @Size(min = 4, max = 20, message = "User ID must be between 4 and 20 characters")
        private String userId;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
        private String password;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "User ID is required")
        private String userId;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Getter
    @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private String userId;
        private String email;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
