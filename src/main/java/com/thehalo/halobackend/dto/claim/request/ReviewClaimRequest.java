package com.thehalo.halobackend.dto.claim.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

// Claims officer approves or denies an open claim
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewClaimRequest {

        @NotBlank(message = "Officer comments are required")
        @Size(min = 20, max = 2000)
        private String officerComments;

        // Required on APPROVE; must be null on DENY
        @DecimalMin(value = "0.01", message = "Approved amount must be positive")
        private BigDecimal approvedAmount;
}
