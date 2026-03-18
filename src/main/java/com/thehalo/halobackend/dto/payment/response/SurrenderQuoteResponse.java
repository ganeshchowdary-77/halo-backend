package com.thehalo.halobackend.dto.payment.response;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SurrenderQuoteResponse {

    private Long policyId;
    private String policyNumber;
    private BigDecimal totalPremiumPaid;
    private BigDecimal guaranteedMaturityBenefit;
    private BigDecimal earlySurrenderValue;
    private String warningMessage;

}
