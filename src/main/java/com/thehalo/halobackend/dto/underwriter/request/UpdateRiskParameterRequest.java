package com.thehalo.halobackend.dto.underwriter.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRiskParameterRequest {
    private BigDecimal multiplier;
    private String description;
    private Boolean active;
    private String modifiedBy;
}
