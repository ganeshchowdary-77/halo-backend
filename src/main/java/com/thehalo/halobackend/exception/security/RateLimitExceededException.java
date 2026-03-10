package com.thehalo.halobackend.exception.security;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;
import lombok.Getter;

@Getter
public class RateLimitExceededException extends BaseException {
    
    private final int retryAfterSeconds;
    
    public RateLimitExceededException(int retryAfterSeconds) {
        super("Rate limit exceeded. Please try again later.", ErrorCode.RATE_LIMIT_EXCEEDED);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
