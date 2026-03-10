package com.thehalo.halobackend.dto.product.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Full detail DTO for admin management or customer pre-purchase view
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {

        private Long id;
        private String name;
        private String tagline;
        // Full product description for detail view
        private String description;
        private BigDecimal basePremium;

        private Boolean coverageLegal;
        // Maximum payout for legal defence claims
        private BigDecimal coverageLimitLegal;

        private Boolean coveragePR;
        // Maximum payout for PR crisis management
        private BigDecimal coverageLimitPR;

        private Boolean coverageMonitoring;
        // Maximum payout for reputational monitoring services
        private BigDecimal coverageLimitMonitoring;

        // Sum of all sub-limits
        private BigDecimal totalCoverageLimit;
        private Boolean active;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // How many influencers currently use this product
        private Long activePolicyCount;
}
