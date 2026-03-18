package com.thehalo.halobackend.exception.domain.claim;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class DocumentNotFoundException extends BaseException {
    public DocumentNotFoundException(Long id) {
        super("Document not found with id: " + id, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
