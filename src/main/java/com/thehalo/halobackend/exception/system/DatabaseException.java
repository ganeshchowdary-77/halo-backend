package com.thehalo.halobackend.exception.system;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class DatabaseException extends BaseException {

    public DatabaseException() {
        super("Database error occurred", ErrorCode.DATABASE_ERROR);
    }
}