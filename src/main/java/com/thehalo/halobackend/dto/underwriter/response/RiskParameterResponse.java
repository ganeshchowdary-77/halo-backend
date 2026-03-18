package com.thehalo.halobackend.dto.underwriter.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskParameterResponse {
    private Long id;
    private String paramKey;
    private BigDecimal multiplier;
    private String description;
    private Boolean active;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
