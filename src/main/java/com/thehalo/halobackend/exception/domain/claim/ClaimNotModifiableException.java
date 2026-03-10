package com.thehalo.halobackend.exception.domain.claim;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

/**
 * Thrown when a Claims Officer or system tries to modify a claim
 * that is no longer in PENDING state (i.e. already APPROVED, DENIED, or
 * CLOSED).
 */
public class ClaimNotModifiableException extends BaseException {
    public ClaimNotModifiableException(Long claimId, String currentStatus) {
        super("Claim id=" + claimId + " cannot be modified — current status is: " + currentStatus,
                ErrorCode.CLAIM_NOT_MODIFIABLE);
    }
}
