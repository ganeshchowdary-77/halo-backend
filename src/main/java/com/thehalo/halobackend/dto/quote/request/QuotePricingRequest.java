package com.thehalo.halobackend.dto.quote.request;

import com.thehalo.halobackend.enums.PlatformName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QuotePricingRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Platform is required")
    private PlatformName platform;
    
    @Positive(message = "Coverage amount must be positive")
    private BigDecimal requestedCoverage;
    
    // Optional - if provided, gives personalized pricing
    private Long influencerProfileId;
}