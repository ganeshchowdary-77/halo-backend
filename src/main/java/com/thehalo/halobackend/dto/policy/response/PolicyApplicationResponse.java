package com.thehalo.halobackend.dto.policy.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.QuoteStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * response for Policy Admin Application Queue
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyApplicationResponse {
    private Long id;
    private String insurerName;
    private String platform;
    private QuoteStatus status;
    private PolicyStatus policyStatus;
    private BigDecimal requestedAmount;
    private Integer riskScore;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;
}
