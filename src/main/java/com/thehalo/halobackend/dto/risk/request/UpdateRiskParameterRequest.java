package com.thehalo.halobackend.dto.risk.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateRiskParameterRequest {

    @NotNull(message = "Multiplier is required")
    @DecimalMin(value = "0.1", message = "Multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Multiplier cannot exceed 10.0")
    private BigDecimal multiplier;

    @NotNull(message = "Update note is required")
    @Size(min = 10, max = 1000, message = "Update note must be between 10 and 1000 characters")
    private String updateNote;
}