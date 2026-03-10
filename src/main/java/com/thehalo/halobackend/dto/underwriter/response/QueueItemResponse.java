package com.thehalo.halobackend.dto.underwriter.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class QueueItemResponse {
    private Long quoteId;
    private String quoteNumber; // Add quote number for better audit trail
    private String influencerName;
    private String influencerEmail;
    private String platform;
    private String niche;
    private Integer followerCount;
    private BigDecimal requestedCoverage;
    private BigDecimal estimatedPremium;
    private String priority;
    private LocalDateTime createdAt;
    private Long timeInQueue; // minutes
    private String assignedUnderwriter;
    private String status;
    private String productName;
}