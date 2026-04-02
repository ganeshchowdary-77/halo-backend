package com.thehalo.halobackend.dto.policy.response;

import com.thehalo.halobackend.enums.PolicyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Full policy detail for customer view or admin management
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyDetailResponse {

        private Long id;
        private String policyNumber;
        private PolicyStatus status;

        // Embedded product info
        private Long productId;
        private String productName;
        private String productTagline;

        // Insured profile
        private Long insuredProfileId;
        private String insuredProfileHandle;
        private String insuredPlatform;

        // Policy holder info
        private String holderName;
        private String holderEmail;

        // Coverage financials
        private BigDecimal totalCoverageLimit;
        private BigDecimal premiumAmount;

        // Per-coverage detail
        private Boolean coverageLegal;
        private BigDecimal limitLegal;
        private Boolean coverageReputation;
        private BigDecimal limitReputation;
        private Boolean coverageCyber;
        private BigDecimal limitCyber;

        // Risk and lifecycle info
        private Integer riskScore;
        private Integer renewalCount;
        private LocalDate startDate;
        private LocalDate endDate;

        // Underwriter who approved the policy
        private String underwriterName;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
}
