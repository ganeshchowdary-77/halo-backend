package com.thehalo.halobackend.dto.product.response;

import com.thehalo.halobackend.enums.ChargeType;
import com.thehalo.halobackend.enums.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomProductResponse {
    private Long id;
    private Long baseProductId;
    private String baseProductName;
    private Long platformId;
    private String platformHandle;
    private List<CustomChargeResponse> customCharges;
    private BigDecimal totalCustomPremium;
    private QuoteStatus status;
    private String underwriterNotes;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomChargeResponse {
        private ChargeType chargeType;
        private BigDecimal chargeAmount;
        private String description;
    }
}