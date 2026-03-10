package com.thehalo.halobackend.exception.business;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class BusinessRuleViolationException extends BaseException {

    public BusinessRuleViolationException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }
}
