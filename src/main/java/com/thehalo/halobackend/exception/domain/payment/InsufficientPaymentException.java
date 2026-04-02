package com.thehalo.halobackend.exception.domain.payment;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

import java.math.BigDecimal;

public class InsufficientPaymentException extends BaseException {
    public InsufficientPaymentException(BigDecimal required, BigDecimal provided) {
        super(
            String.format("Payment amount $%s is insufficient. Required: $%s", provided, required),
            ErrorCode.INSUFFICIENT_PAYMENT
        );
    }
}
