package com.thehalo.halobackend.dto.product.response;

import com.thehalo.halobackend.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeTypeOptionResponse {
    private ChargeType chargeType;
    private String name;
    private String description;
    private String billingType;
    private BigDecimal estimatedCost;
}