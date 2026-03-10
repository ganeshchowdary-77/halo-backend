package com.thehalo.halobackend.dto.risk.response;

import com.thehalo.halobackend.enums.Niche;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RiskParameterResponse {
    private Long id;
    private String paramKey;
    private String label;
    private BigDecimal multiplier;
    private String description;
    private Niche applicableNiche;
    private String updateNote;
    private String updatedByUserName;
    private LocalDateTime updatedAt;
    private Boolean active;
}