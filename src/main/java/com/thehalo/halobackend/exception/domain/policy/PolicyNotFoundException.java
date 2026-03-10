package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class PolicyNotFoundException extends BaseException {
    public PolicyNotFoundException(Long id) {
        super("Policy not found with id: " + id, ErrorCode.POLICY_NOT_FOUND);
    }

    public PolicyNotFoundException(String policyNumber) {
        super("Policy not found: " + policyNumber, ErrorCode.POLICY_NOT_FOUND);
    }
}
