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
        // Monthly base premium
        private BigDecimal basePremium;

        private java.util.List<String> keyFeatures;

        private Boolean coverageLegal;
        // Maximum payout for legal defence claims
        private BigDecimal coverageLimitLegal;

        private Boolean coverageReputation;
        // Maximum payout for PR crisis management
        private BigDecimal coverageLimitReputation;

        private Boolean coverageCyber;
        // Maximum payout for cyber recovery services
        private BigDecimal coverageLimitCyber;

        // Sum of all sub-limits
        private BigDecimal totalCoverageLimit;
        // Alias for totalCoverageLimit for consistency with PublicProductResponse
        private BigDecimal coverageAmount;
        private Boolean active;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // How many influencers currently use this product
        private Long activePolicyCount;
}
