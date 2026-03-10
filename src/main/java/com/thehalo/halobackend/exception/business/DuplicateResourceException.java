package com.thehalo.halobackend.exception.business;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String resource) {
        super(resource + " already exists", ErrorCode.DUPLICATE_RESOURCE);
    }
}
