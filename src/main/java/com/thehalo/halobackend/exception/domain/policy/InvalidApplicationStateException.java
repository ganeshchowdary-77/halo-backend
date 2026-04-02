package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InvalidApplicationStateException extends BaseException {
    public InvalidApplicationStateException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }
}
