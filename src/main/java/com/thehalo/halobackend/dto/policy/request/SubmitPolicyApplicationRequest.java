package com.thehalo.halobackend.dto.policy.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for submitting a policy application.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitPolicyApplicationRequest {

    @NotNull(message = "Profile ID is required")
    private Long profileId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String notes;

    // Security Assessment fields
    private Boolean hasTwoFactorAuth;
    private String passwordRotationFrequency;   // NEVER, MONTHLY, YEARLY
    private Boolean thirdPartyManagement;
    private String sponsoredContentFrequency;   // NONE, OCCASIONAL, FREQUENT
}
