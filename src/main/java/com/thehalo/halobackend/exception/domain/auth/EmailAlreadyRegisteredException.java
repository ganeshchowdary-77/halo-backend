package com.thehalo.halobackend.exception.domain.auth;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class EmailAlreadyRegisteredException extends BaseException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email is already registered: " + email, ErrorCode.EMAIL_ALREADY_REGISTERED);
    }
}
