package com.thehalo.halobackend.dto.policy.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thehalo.halobackend.enums.PolicyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed response for a policy application.
 * Includes calculated premium, risk assessment, and policy reference if auto-approved.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyApplicationDetailResponse {

    private Long id;
    private String applicationNumber;
    private PolicyStatus status;

    // Product info
    private Long productId;
    private String productName;
    private String productTagline;
    private BigDecimal basePremium;

    // Profile info
    private Long profileId;
    private String profileHandle;
    private String platformName;
    private Integer followerCount;
    private BigDecimal engagementRate;
    private String niche;
    
    // User info
    private String influencerName;

    // Security assessment
    private Boolean hasTwoFactorAuth;
    private String passwordRotationFrequency;
    private Boolean thirdPartyManagement;
    private String sponsoredContentFrequency;

    // Auto-calculated results
    private BigDecimal calculatedPremium;
    private Integer riskScore;
    private String riskLevel;       // LOW, MEDIUM, HIGH
    private Boolean requiresReview;

    // Policy reference (populated when approved)
    private Long policyId;

    // Review info (for high-risk applications)
    private String assignedUnderwriterName;
    private String underwriterNotes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewedAt;

    private String notes;
}
