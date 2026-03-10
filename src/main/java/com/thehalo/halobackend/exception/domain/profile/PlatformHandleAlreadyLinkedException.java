package com.thehalo.halobackend.exception.domain.profile;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class PlatformHandleAlreadyLinkedException extends BaseException {
    public PlatformHandleAlreadyLinkedException(String handle) {
        super("This platform handle is already linked to an account: " + handle,
                ErrorCode.PLATFORM_HANDLE_ALREADY_LINKED);
    }
}
