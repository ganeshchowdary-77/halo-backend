package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ApplicationNotFoundException extends BaseException {
    public ApplicationNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ApplicationNotFoundException(Long applicationId) {
        super("Application with ID " + applicationId + " not found", ErrorCode.RESOURCE_NOT_FOUND);
    }
}
