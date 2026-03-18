package com.thehalo.halobackend.exception.system;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class FileUploadException extends BaseException {
    public FileUploadException(String message) {
        super(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
