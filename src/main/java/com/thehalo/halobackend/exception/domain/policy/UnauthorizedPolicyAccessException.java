package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class UnauthorizedPolicyAccessException extends BaseException {
    public UnauthorizedPolicyAccessException(Long policyId, Long userId) {
        super(
            String.format("User %d is not authorized to access policy %d", userId, policyId),
            ErrorCode.UNAUTHORIZED_ACCESS
        );
    }
}
