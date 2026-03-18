package com.thehalo.halobackend.exception.business;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

/**
 * Base class for all business logic exceptions.
 * Can be used directly for generic business errors or extended for specific ones.
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }

    public BusinessException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
