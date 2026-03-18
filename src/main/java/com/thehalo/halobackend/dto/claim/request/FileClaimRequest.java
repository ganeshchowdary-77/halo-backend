package com.thehalo.halobackend.dto.claim.request;

import com.thehalo.halobackend.enums.ExpenseType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// Influencer files a new claim against their active policy
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileClaimRequest {

        @NotNull(message = "Policy ID is required")
        private Long policyId;

        // Profile ID is now optional - we get it from the policy
        private Long profileId;

        @NotNull(message = "Incident date is required")
        @PastOrPresent(message = "Incident date cannot be in the future")
        private LocalDate incidentDate;

        @NotBlank(message = "Description is required")
        @Size(min = 50, max = 3000)
        private String description;

        @Size(max = 2048)
        private String incidentUrl;

        @NotNull(message = "Expense type is required")
        private ExpenseType expenseType;

        @NotNull(message = "Claim amount is required")
        @DecimalMin(value = "100.00", message = "Minimum claim amount is $100")
        private BigDecimal claimAmount;
}
