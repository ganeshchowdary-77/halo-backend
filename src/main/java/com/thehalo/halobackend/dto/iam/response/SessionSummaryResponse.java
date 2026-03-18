package com.thehalo.halobackend.dto.iam.response;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Response DTO representing a single active login session.
 * Built from RefreshToken + AppUser data.
 */
@Value
@Builder
public class SessionSummaryResponse {

    Long   tokenId;
    String email;
    String fullName;
    String role;
    LocalDateTime issuedAt;
    LocalDateTime expiresAt;
    boolean revoked;
}
