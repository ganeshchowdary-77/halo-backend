package com.thehalo.halobackend.exception.business;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(message, ErrorCode.USER_NOT_FOUND);
    }
    
    public UserNotFoundException(Long userId) {
        super("User with ID " + userId + " not found", ErrorCode.USER_NOT_FOUND);
    }
}