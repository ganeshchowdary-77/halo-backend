package com.thehalo.halobackend.exception.domain.underwriter;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class RiskParameterNotFoundException extends BaseException {
    public RiskParameterNotFoundException(Long id) {
        super("Risk parameter configuration not found with id: " + id, ErrorCode.RISK_PARAMETER_NOT_FOUND);
    }
}
