package com.thehalo.halobackend.dto.product.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull(message = "coveredLegal is required")
    private Boolean coveredLegal;

    @NotNull(message = "coveredReputation is required")
    private Boolean coveredReputation;

    @NotNull(message = "coveredCyber is required")
    private Boolean coveredCyber;

    @DecimalMin(value = "0.0", inclusive = true, message = "coverageLimitLegal must be non-negative")
    private BigDecimal coverageLimitLegal;

    @DecimalMin(value = "0.0", inclusive = true, message = "coverageLimitReputation must be non-negative")
    private BigDecimal coverageLimitReputation;

    @DecimalMin(value = "0.0", inclusive = true, message = "coverageLimitCyber must be non-negative")
    private BigDecimal coverageLimitCyber;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePremium;

    private String tagline;

}