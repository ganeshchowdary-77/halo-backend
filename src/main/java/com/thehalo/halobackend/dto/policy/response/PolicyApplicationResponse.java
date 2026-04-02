package com.thehalo.halobackend.dto.policy.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thehalo.halobackend.enums.PolicyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary response for Policy Application list views (admin & underwriter).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyApplicationResponse {
    private Long id;
    private String applicationNumber;
    private String insurerName;
    private String platform;
    private PolicyStatus status;
    private BigDecimal calculatedPremium;
    private Integer riskScore;
    private String productName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedAt;
}
