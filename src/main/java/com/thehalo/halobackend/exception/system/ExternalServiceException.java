package com.thehalo.halobackend.exception.system;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ExternalServiceException extends BaseException {

    public ExternalServiceException() {
        super("External service unavailable", ErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}