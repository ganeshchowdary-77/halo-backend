package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super("Access denied", ErrorCode.FORBIDDEN);
    }
}
