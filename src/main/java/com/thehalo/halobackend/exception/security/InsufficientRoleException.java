package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InsufficientRoleException extends BaseException {
    public InsufficientRoleException(String message) {
        super(message, ErrorCode.INSUFFICIENT_ROLE);
    }
    
    public InsufficientRoleException(String requiredRole, String actualRole) {
        super("Insufficient role. Required: " + requiredRole + ", Actual: " + actualRole, ErrorCode.INSUFFICIENT_ROLE);
    }
}