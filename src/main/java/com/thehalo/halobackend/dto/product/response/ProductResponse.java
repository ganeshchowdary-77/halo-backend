package com.thehalo.halobackend.dto.product.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean coveredLegal;
    private Boolean coveredReputation;
    private Boolean coveredCyber;

    private BigDecimal coverageLimitLegal;
    private BigDecimal coverageLimitReputation;
    private BigDecimal coverageLimitCyber;


    private BigDecimal basePremium;
    private Boolean active;
}