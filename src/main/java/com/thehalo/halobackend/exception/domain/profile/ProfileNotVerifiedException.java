package com.thehalo.halobackend.exception.domain.profile;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ProfileNotVerifiedException extends BaseException {
    public ProfileNotVerifiedException(Long profileId) {
        super("Profile with ID " + profileId + " is not verified", ErrorCode.PROFILE_NOT_VERIFIED);
    }
}
