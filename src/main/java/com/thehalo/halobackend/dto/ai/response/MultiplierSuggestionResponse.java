package com.thehalo.halobackend.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiplierSuggestionResponse {
    private double suggestedMultiplier;
    private String reasoning;
    private String industryBenchmark;
    private String confidenceLevel;
    private boolean aiGenerated;
}
