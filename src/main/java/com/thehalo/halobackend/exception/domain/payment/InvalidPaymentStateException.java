package com.thehalo.halobackend.exception.domain.payment;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InvalidPaymentStateException extends BaseException {
    public InvalidPaymentStateException(String message) {
        super(
            message,
            ErrorCode.INVALID_PAYMENT_STATE
        );
    }
}
