package com.thehalo.halobackend.service.product;

import com.thehalo.halobackend.dto.product.response.PublicProductResponse;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;

import java.util.List;

public interface PublicProductService {
    List<PublicProductResponse> getPublicProducts();
    ProductDetailResponse getPublicProductDetail(Long productId);
}