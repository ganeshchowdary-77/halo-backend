package com.thehalo.halobackend.exception.base;

import com.thehalo.halobackend.exception.codes.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException{

    private final ErrorCode errorCode;

    protected BaseException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
