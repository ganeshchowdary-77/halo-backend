package com.thehalo.halobackend.dto.quote.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

// Influencer requests a quote from an underwriter
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitQuoteRequest {

    @NotNull(message = "Profile ID is required")
    private Long profileId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}
