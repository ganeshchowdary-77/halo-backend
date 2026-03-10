package com.thehalo.halobackend.exception.domain.auth;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier, ErrorCode.USER_NOT_FOUND);
    }
}
