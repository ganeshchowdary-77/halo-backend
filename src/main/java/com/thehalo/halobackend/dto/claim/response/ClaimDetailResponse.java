package com.thehalo.halobackend.dto.claim.response;

import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.enums.ExpenseType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Full claim detail for influencer review or officer investigation screen
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDetailResponse {

        private Long id;
        private String claimNumber;
        private ClaimStatus status;
        private ExpenseType expenseType;
        private BigDecimal claimAmount;
        private BigDecimal approvedAmount;
        // Full incident description
        private String description;
        // URL to defamatory content if provided
        private String incidentUrl;
        private LocalDate incidentDate;
        private LocalDateTime filedAt;
        private LocalDateTime reviewedAt;

        // Assigned claims officer
        private String assignedOfficerName;
        // Officer's decision notes visible to influencer
        private String officerComments;

        // Policy context
        private String policyNumber;
        private String productName;
        private BigDecimal policyTotalCoverageLimit;

        // Insured profile
        private String profileHandle;
        private String profilePlatform;

        // Supporting documents uploaded with the claim
        private List<ClaimDocumentResponse> documents;
        // Status change audit trail
        private List<ClaimTimelineResponse> timeline;
}
