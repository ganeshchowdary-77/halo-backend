package com.thehalo.halobackend.dto.policy.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        private Long insuredProfileId;
        private String platformName;
        private String insuredProfileHandle;
        private BigDecimal premiumAmount;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
        // Whether policy can be renewed
        private Boolean renewalEligible;
}
