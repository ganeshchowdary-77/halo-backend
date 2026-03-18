package com.thehalo.halobackend.exception.domain.iam;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class StaffNotFoundException extends BaseException {
    public StaffNotFoundException(Long id) {
        super("Staff member not found with id: " + id, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public StaffNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
