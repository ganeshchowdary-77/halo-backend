package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class RefreshTokenExpiredException extends BaseException {
    
    public RefreshTokenExpiredException(String message) {
        super(message, ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
    
    public RefreshTokenExpiredException() {
        super("Refresh token has expired", ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}
