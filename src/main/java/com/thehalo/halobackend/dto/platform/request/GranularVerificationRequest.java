package com.thehalo.halobackend.dto.platform.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for granular verification of individual aspects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GranularVerificationRequest {
    private String verificationType; // "NICHE", "ADDRESS", "INCOME"
    private Boolean approved; // true for verify, false for reject
    private String rejectionReason; // Required if approved = false
}
