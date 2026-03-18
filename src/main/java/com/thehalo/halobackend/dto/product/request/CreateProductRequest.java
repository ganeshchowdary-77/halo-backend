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

    @NotNull
    private Boolean coveredLegal;

    @NotNull
    private Boolean coveredPR;

    @NotNull
    private Boolean coveredMonitoring;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal coverageLimitLegal;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal coverageLimitPR;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal coverageLimitMonitoring;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePremium;

    private String tagline;

}