package com.thehalo.halobackend.dto.product.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    private String name;
    private String description;
    private Boolean coveredLegal;
    private Boolean coveredReputation;
    private Boolean coveredCyber;

    @DecimalMin(value = "0.0", inclusive = true, message = "coverageLimitLegal must be non-negative")
    private BigDecimal coverageLimitLegal;

    @DecimalMin(value = "0.0", inclusive = true, message = "coverageLimitReputation must be non-negative")
    private BigDecimal coverageLimitReputation;

    @DecimalMin(value = "0.0", inclusive = true, message = "coverageLimitCyber must be non-negative")
    private BigDecimal coverageLimitCyber;

    private BigDecimal basePremium;
    private Boolean active;
    private String tagline;
}
