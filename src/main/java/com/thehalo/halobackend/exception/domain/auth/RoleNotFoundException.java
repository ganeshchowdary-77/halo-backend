package com.thehalo.halobackend.exception.domain.auth;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class RoleNotFoundException extends BaseException {
    public RoleNotFoundException(String roleName) {
        super("Role not found: " + roleName, ErrorCode.ROLE_NOT_FOUND);
    }
}
