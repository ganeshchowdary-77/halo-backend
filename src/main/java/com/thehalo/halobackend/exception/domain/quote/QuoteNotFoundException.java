package com.thehalo.halobackend.exception.domain.quote;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class QuoteNotFoundException extends BaseException {
    public QuoteNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }
    
    public QuoteNotFoundException(Long quoteId) {
        super("Quote with ID " + quoteId + " not found", ErrorCode.RESOURCE_NOT_FOUND);
    }
}
