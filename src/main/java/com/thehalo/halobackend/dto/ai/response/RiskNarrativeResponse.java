package com.thehalo.halobackend.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskNarrativeResponse {
    private String narrative;
    private List<RiskFactor> riskFactors;
    private String recommendation;
    private String confidenceLevel;
    private boolean aiGenerated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskFactor {
        private String factor;
        private String severity; // LOW, MEDIUM, HIGH
        private String description;
    }
}
