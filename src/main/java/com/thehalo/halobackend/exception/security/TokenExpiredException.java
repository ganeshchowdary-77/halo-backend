package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class TokenExpiredException extends BaseException {

    public TokenExpiredException() {
        super("Session expired. Please login again.", ErrorCode.TOKEN_EXPIRED);
    }
}
