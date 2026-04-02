package com.thehalo.halobackend.exception.system;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class AiServiceException extends BaseException {
    public AiServiceException(String message) {
        super(message, ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    public AiServiceException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
