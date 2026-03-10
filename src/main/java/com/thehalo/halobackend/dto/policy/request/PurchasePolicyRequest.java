package com.thehalo.halobackend.dto.policy.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

// Request to purchase a new insurance policy for a specific social profile
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasePolicyRequest {

        @NotNull(message = "Profile ID is required")
        private Long profileId;

        @NotNull(message = "Product ID is required")
        private Long productId;
}
