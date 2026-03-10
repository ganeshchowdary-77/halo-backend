package com.thehalo.halobackend.dto.payment.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

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
