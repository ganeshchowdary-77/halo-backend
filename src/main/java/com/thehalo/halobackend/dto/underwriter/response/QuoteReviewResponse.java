package com.thehalo.halobackend.dto.underwriter.response;

import com.thehalo.halobackend.enums.QuoteStatus;
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
public class QuoteReviewResponse {
    // Quote Info
    private Long quoteId;
    private String quoteNumber;
    private String status; // Changed to String for frontend compatibility if needed, or keep QuoteStatus
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private String notes;
    
    // User Info
    private Long userId;
    private String userEmail;
    private String influencerName;
    private String influencerEmail;
    private String priority;
    
    // Profile Info
    private Long profileId;
    private String profileHandle;
    private String platform;
    private Integer followerCount;
    private BigDecimal engagementRate;
    private String niche;
    
    // Product Info
    private Long productId;
    private String productName;
    private BigDecimal requestedCoverage;
    
    // Pricing Info
    private BigDecimal calculatedPremium;
    private BigDecimal estimatedPremium;
    private BigDecimal offeredPremium;
    
    // Review Info
    private String reviewReason;
}
