package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super("Invalid email or password", ErrorCode.INVALID_CREDENTIALS);
    }
    
    public InvalidCredentialsException(Throwable cause) {
        super("Invalid email or password", ErrorCode.INVALID_CREDENTIALS);
        initCause(cause);
    }
}
