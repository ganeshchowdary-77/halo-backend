package com.thehalo.halobackend.dto.risk.request;

import com.thehalo.halobackend.enums.Niche;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRiskParameterRequest {
    
    @NotBlank(message = "Parameter key is required")
    @Size(min = 3, max = 50, message = "Parameter key must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Parameter key must contain only uppercase letters and underscores")
    private String paramKey;
    
    @NotBlank(message = "Label is required")
    @Size(min = 3, max = 100, message = "Label must be between 3 and 100 characters")
    private String label;
    
    @NotNull(message = "Multiplier is required")
    @DecimalMin(value = "0.1", message = "Multiplier must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Multiplier must not exceed 10.0")
    private BigDecimal multiplier;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Niche applicableNiche;
    
    @NotBlank(message = "Update note is required")
    @Size(min = 10, max = 1000, message = "Update note must be between 10 and 1000 characters")
    private String updateNote;
}