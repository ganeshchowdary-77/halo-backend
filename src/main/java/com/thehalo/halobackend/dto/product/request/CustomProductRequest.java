package com.thehalo.halobackend.dto.product.request;

import com.thehalo.halobackend.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomProductRequest {
    private Long baseProductId;
    private Long platformId;
    private List<CustomChargeRequest> customCharges;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomChargeRequest {
        private ChargeType chargeType;
        private BigDecimal chargeAmount;
        private String description;
    }
}