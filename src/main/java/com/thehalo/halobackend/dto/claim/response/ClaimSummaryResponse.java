package com.thehalo.halobackend.dto.claim.response;

import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.enums.ExpenseType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Lightweight row DTO for "My Claims" list and officer claim queue
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimSummaryResponse {

        private Long id;
        private String claimNumber;
        private ClaimStatus status;
        private ExpenseType expenseType;
        private BigDecimal claimAmount;
        // Null until officer makes a decision
        private BigDecimal approvedAmount;
        private LocalDate incidentDate;
        // ISO date-time string of when the claim was filed
        private String filedAt;
        // Context for officer queue
        private String policyNumber;
        // Which social profile was affected
        private String profileHandle;
}
