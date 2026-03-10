package com.thehalo.halobackend.dto.auth.request;

import jakarta.validation.constraints.*;
import lombok.*;

// Registration DTO — all self-registered users become INFLUENCER
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
                 message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
        private String password;
}
