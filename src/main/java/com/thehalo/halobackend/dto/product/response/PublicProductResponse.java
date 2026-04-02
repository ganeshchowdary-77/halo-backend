package com.thehalo.halobackend.dto.product.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicProductResponse {
    private Long id;
    private String name;
    private String tagline;
    private String description;
    private BigDecimal basePremium;
    private BigDecimal coverageAmount;
    private Boolean coveredLegal;
    private Boolean coveredReputation;
    private Boolean coveredCyber;
    private BigDecimal coverageLimitLegal;
    private BigDecimal coverageLimitReputation;
    private BigDecimal coverageLimitCyber;
    private List<String> features;
    private boolean popular;
    private String marketingMessage;
}