package com.thehalo.halobackend.exception.domain.claim;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

/**
 * Thrown when the filed claim amount exceeds the policy's total coverage limit.
 * Provides the amount and limit for meaningful error messages.
 */
public class ClaimAmountExceedsCoverageException extends BaseException {
    public ClaimAmountExceedsCoverageException(Double requestedAmount, Double coverageLimit) {
        super(String.format(
                "Claim amount $%.2f exceeds the policy coverage limit of $%.2f",
                requestedAmount, coverageLimit), ErrorCode.CLAIM_AMOUNT_EXCEEDS_COVERAGE);
    }
}
