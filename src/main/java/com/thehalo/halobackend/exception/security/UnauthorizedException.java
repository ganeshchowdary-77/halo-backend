package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super("Unauthorized access", ErrorCode.UNAUTHORIZED);
    }
}