package com.thehalo.halobackend.dto.quote.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstantPriceResponse {
    private Long productId;
    private String productName;
    private Long profileId;
    private String profileHandle;
    private String platformName;
    private Integer followerCount;
    private BigDecimal calculatedPremium;
    private String billingCycle;
    private Boolean requiresUnderwriterReview;
    private BigDecimal coverageAmount;
    private String message;
}
