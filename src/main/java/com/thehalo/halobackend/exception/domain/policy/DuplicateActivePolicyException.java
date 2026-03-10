package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

/**
 * Thrown when an influencer profile already has an active policy for the
 * requested product.
 */
public class DuplicateActivePolicyException extends BaseException {
    public DuplicateActivePolicyException(Long profileId, Long productId) {
        super("Profile id=" + profileId + " already has an active policy for product id=" + productId,
                ErrorCode.DUPLICATE_ACTIVE_POLICY);
    }
}
