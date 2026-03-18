package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InvalidPolicyStateException extends BaseException {
    public InvalidPolicyStateException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }
}
