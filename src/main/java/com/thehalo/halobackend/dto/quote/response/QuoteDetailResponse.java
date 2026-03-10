package com.thehalo.halobackend.dto.quote.response;

import com.thehalo.halobackend.enums.QuoteStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Full projection for a specific Quote
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteDetailResponse {

    private Long id;
    private String quoteNumber;
    private QuoteStatus status;

    // Product details
    private Long productId;
    private String productName;
    private String productTagline;
    private BigDecimal basePremium;

    // Profile details
    private String profileHandle;
    private String platformName;
    private Long followerCount;
    private Double engagementRate;
    private Integer riskScore;

    // Quote specific payload
    private String notes;
    private BigDecimal offeredPremium;
    private String underwriterNotes;
    private String assignedUnderwriterName;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
