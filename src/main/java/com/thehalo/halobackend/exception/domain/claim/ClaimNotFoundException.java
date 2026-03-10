package com.thehalo.halobackend.exception.domain.claim;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ClaimNotFoundException extends BaseException {
    public ClaimNotFoundException(Long id) {
        super("Claim not found with id: " + id, ErrorCode.CLAIM_NOT_FOUND);
    }

    public ClaimNotFoundException(String claimNumber) {
        super("Claim not found: " + claimNumber, ErrorCode.CLAIM_NOT_FOUND);
    }
}
