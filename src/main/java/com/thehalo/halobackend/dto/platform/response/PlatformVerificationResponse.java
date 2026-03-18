package com.thehalo.halobackend.dto.platform.response;

import com.thehalo.halobackend.enums.PlatformVerificationStatus;
import com.thehalo.halobackend.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformVerificationResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String influencerName; // User's full name
    private String influencerEmail; // User's email (duplicate of userEmail for consistency)
    private String platformName;
    private String handle;
    private String platformUrl;
    private Integer followerCount;
    private BigDecimal engagementRate;
    private String niche;
    private Boolean verified;
    private PlatformVerificationStatus verificationStatus;
    private RiskLevel riskLevel;
    private String verificationNotes;
    private String rejectionReason;
    private String addressProofPath;
    private String addressProofUrl; // Full URL for frontend
    private String idVerificationPath;
    private String incomeProofPath;
    private String incomeProofUrl; // Full URL for frontend
    private Integer previousClaimsCount;
    private BigDecimal previousClaimsAmount;
    private LocalDateTime verifiedAt;
    private String verifiedByEmail;
    private LocalDateTime createdAt;
    
    // Granular verification fields
    private Boolean nicheVerified;
    private String nicheRejectionReason;
    private Boolean addressVerified;
    private String addressRejectionReason;
    private Boolean incomeVerified;
    private String incomeRejectionReason;
}