package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

/**
 * Thrown when a non-ACTIVE policy is used to file a claim or perform a
 * restricted action.
 */
public class PolicyNotActiveException extends BaseException {
    public PolicyNotActiveException(Long policyId) {
        super("Policy id=" + policyId + " is not in ACTIVE state", ErrorCode.POLICY_NOT_ACTIVE);
    }
}
