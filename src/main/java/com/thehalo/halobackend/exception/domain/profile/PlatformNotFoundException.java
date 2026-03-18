package com.thehalo.halobackend.exception.domain.profile;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class PlatformNotFoundException extends BaseException {
    public PlatformNotFoundException(Long id) {
        super("Social media platform not found with id: " + id, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
