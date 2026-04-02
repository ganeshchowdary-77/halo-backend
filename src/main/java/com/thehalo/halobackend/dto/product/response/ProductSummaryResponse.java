package com.thehalo.halobackend.dto.product.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

// Summary DTO for public landing page and product listing cards
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryResponse {

        private Long id;
        private String name;
        // Short tagline shown on pricing card
        private String tagline;
        // Monthly base premium
        private BigDecimal basePremium;
        // Human-readable bullet points, e.g. "Legal cover up to $500k"
        private List<String> keyFeatures;
        private Boolean coverageLegal;
        private Boolean coverageReputation;
        private Boolean coverageCyber;
}
