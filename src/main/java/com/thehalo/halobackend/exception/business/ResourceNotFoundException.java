package com.thehalo.halobackend.exception.business;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resource) {
        super(resource + " not found", ErrorCode.RESOURCE_NOT_FOUND);
    }
}
