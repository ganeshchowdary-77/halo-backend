package com.thehalo.halobackend.exception.domain.product;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ProductNotAvailableException extends BaseException {
    public ProductNotAvailableException(Long productId) {
        super("Product with ID " + productId + " is not available", ErrorCode.PRODUCT_NOT_AVAILABLE);
    }
}
