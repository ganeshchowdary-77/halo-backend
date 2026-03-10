package com.thehalo.halobackend.dto.policy.response;

import com.thehalo.halobackend.enums.PolicyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Lightweight DTO for influencer coverage card widget
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicySummaryResponse {

        private Long id;
        private String policyNumber;
        private PolicyStatus status;
        // Product name e.g. "Halo Elite"
        private String productName;
        private BigDecimal premiumAmount;
        private LocalDate startDate;
        private LocalDate endDate;
        // Whether policy can be renewed
        private Boolean renewalEligible;
}
