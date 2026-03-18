package com.thehalo.halobackend.exception.domain.payment;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class PaymentMethodNotFoundException extends BaseException {
    public PaymentMethodNotFoundException(Long id) {
        super("Payment method not found with ID: " + id, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public PaymentMethodNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
