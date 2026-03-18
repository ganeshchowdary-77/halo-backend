package com.thehalo.halobackend.exception.domain.quote;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InvalidQuoteStateException extends BaseException {
    public InvalidQuoteStateException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }
}
