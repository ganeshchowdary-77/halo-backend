package com.thehalo.halobackend.exception.domain.policy;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

/** Thrown when a requested product is inactive / not yet published for sale. */
public class ProductNotAvailableException extends BaseException {
    public ProductNotAvailableException(Long productId) {
        super("Insurance product id=" + productId + " is not currently available for purchase",
                ErrorCode.PRODUCT_NOT_AVAILABLE);
    }
}
