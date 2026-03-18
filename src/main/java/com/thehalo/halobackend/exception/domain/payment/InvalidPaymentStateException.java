package com.thehalo.halobackend.exception.domain.payment;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidPaymentStateException extends BaseException {
    public InvalidPaymentStateException(String message) {
        super(
            message,
            ErrorCode.INVALID_PAYMENT_STATE
        );
    }
}
