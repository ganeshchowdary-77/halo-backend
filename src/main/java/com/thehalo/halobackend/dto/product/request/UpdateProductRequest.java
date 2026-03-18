package com.thehalo.halobackend.dto.product.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    private String name;
    private String description;
    private Boolean coveredLegal;
    private Boolean coveredPR;
    private Boolean coveredMonitoring;
    private BigDecimal coverageLimitLegal;
    private BigDecimal coverageLimitPR;
    private BigDecimal coverageLimitMonitoring;
    private BigDecimal basePremium;
    private Boolean active;
    private String tagline;
}
