package com.thehalo.halobackend.exception.domain.product;

import com.thehalo.halobackend.exception.base.BaseException;
import com.thehalo.halobackend.exception.codes.ErrorCode;

public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ProductNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
