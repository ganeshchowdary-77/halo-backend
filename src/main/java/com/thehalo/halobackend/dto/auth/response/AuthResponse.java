package com.thehalo.halobackend.dto.auth.response;

import lombok.*;

// JWT + user metadata returned on login or register success
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    // Always "Bearer"
    private String tokenType;
    // Token validity in seconds
    private long expiresIn;
    private Long userId;
    private String fullName;
    private String email;
    // INFLUENCER | IAM_ADMIN | POLICY_ADMIN | CLAIMS_OFFICER | UNDERWRITER
    private String role;
}
