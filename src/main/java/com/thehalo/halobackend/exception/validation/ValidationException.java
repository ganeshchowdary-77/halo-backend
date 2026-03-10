package com.thehalo.halobackend.exception.validation;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(message, ErrorCode.VALIDATION_ERROR);
    }
}
