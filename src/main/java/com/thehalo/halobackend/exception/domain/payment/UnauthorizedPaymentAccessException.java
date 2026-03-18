package com.thehalo.halobackend.exception.domain.payment;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class UnauthorizedPaymentAccessException extends BaseException {
    public UnauthorizedPaymentAccessException(Long resourceId, Long userId) {
        super(
            String.format("User %d is not authorized to access payment resource %d", userId, resourceId),
            ErrorCode.UNAUTHORIZED_ACCESS
        );
    }
}
