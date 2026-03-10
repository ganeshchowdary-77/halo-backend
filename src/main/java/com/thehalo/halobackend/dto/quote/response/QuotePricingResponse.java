package com.thehalo.halobackend.dto.quote.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class QuotePricingResponse {
    private BigDecimal startingFromPrice;
    private BigDecimal personalizedPrice;
    private String displayText;
    private boolean requiresProfile;
    private boolean requiresUnderwriter;
    private String riskLevel;
    private String platform;
    private String productName;
}