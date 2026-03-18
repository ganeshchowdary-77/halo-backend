package com.thehalo.halobackend.dto.payment.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryResponse {

    private Long policyId;
    private String policyNumber;
    private BigDecimal basePremiumDue;
    private BigDecimal lateFeesDue;
    private BigDecimal totalAmountDue;
    private Integer daysOverdue;
    private LocalDate nextPaymentDueDate;

}
