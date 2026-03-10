package com.thehalo.halobackend.exception.system;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class InternalServerException extends BaseException {

    public InternalServerException() {
        super("Something went wrong. Please try again later.",
                ErrorCode.INTERNAL_SERVER_ERROR);
    }
}