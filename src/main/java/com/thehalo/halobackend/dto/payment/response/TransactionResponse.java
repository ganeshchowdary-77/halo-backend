package com.thehalo.halobackend.dto.payment.response;

import com.thehalo.halobackend.enums.TransactionStatus;
import com.thehalo.halobackend.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private Long id;
    private String policyNumber;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String paymentMethodLast4;
    private String referenceNumber;
    private LocalDateTime transactionDate;

}
