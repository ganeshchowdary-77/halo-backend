package com.thehalo.halobackend.exception.domain.profile;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ProfileNotFoundException extends BaseException {
    public ProfileNotFoundException(Long id) {
        super("Social media profile not found with id: " + id, ErrorCode.PROFILE_NOT_FOUND);
    }
}
