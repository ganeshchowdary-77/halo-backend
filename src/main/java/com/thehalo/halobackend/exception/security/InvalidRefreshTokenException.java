package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InvalidRefreshTokenException extends BaseException {
    
    public InvalidRefreshTokenException(String message) {
        super(message, ErrorCode.INVALID_REFRESH_TOKEN);
    }
    
    public InvalidRefreshTokenException() {
        super("Invalid refresh token", ErrorCode.INVALID_REFRESH_TOKEN);
    }
}
