package com.thehalo.halobackend.exception.domain.payment;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class PaymentProcessingException extends BaseException {
    public PaymentProcessingException(String message) {
        super(
            message,
            ErrorCode.PAYMENT_PROCESSING_FAILED
        );
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, ErrorCode.PAYMENT_PROCESSING_FAILED);
        initCause(cause);
    }
}
