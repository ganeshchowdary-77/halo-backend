package com.thehalo.halobackend.dto.quote.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

// Underwriter approves or denies a quote
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewQuoteRequest {

    // Required if approved
    @DecimalMin(value = "0.01", message = "Offered premium must be positive")
    private BigDecimal offeredPremium;

    @Size(max = 2000, message = "Underwriter notes must not exceed 2000 characters")
    private String underwriterNotes;
}
