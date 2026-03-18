package com.thehalo.halobackend.dto.underwriter.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Detailed premium calculation breakdown
 * Shows exactly how the premium was calculated step by step
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumCalculationLogResponse {
    // Quote identification
    private Long quoteId;
    private String quoteNumber;
    
    // Base calculation
    private BigDecimal basePremium;
    
    // Platform adjustment
    private BigDecimal platformMultiplier;
    private String platformName;
    private BigDecimal afterPlatform;
    
    // Follower count adjustment
    private BigDecimal followerMultiplier;
    private Integer followerCount;
    private BigDecimal afterFollower;
    
    // Engagement rate adjustment
    private BigDecimal engagementMultiplier;
    private BigDecimal engagementRate;
    private BigDecimal afterEngagement;
    
    // Niche risk adjustment
    private BigDecimal nicheMultiplier;
    private String niche;
    private BigDecimal afterNiche;
    
    // Coverage amount adjustment
    private BigDecimal coverageMultiplier;
    private BigDecimal coverageAmount;
    
    // Final result
    private BigDecimal finalPremium;
    
    // Risk assessment
    private Integer riskScore; // 0-100
    private String riskLevel; // LOW, MEDIUM, HIGH
    
    // Audit information
    private String assignedUnderwriterName;
    private String reviewedBy; // Name of underwriter who approved/rejected
    private String reviewedAt; // Timestamp of approval/rejection
    
    // Human-readable calculation steps
    private List<String> calculationSteps;
}
