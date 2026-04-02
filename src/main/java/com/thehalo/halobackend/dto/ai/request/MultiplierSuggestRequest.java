package com.thehalo.halobackend.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiplierSuggestRequest {
    private String paramKey;
    private String description;
}
