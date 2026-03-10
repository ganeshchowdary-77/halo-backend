package com.thehalo.halobackend.service.product;

import com.thehalo.halobackend.dto.product.request.CreateProductRequest;
import com.thehalo.halobackend.dto.product.request.UpdateProductRequest;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;

import java.util.List;

// Product management operations for admin and public listing
public interface ProductService {

    // Public: landing page listing cards
    List<ProductSummaryResponse> getActiveSummaries();

    // Authenticated: full detail view
    ProductDetailResponse getDetail(Long id);

    // Admin only: create, update, delete
    ProductDetailResponse create(CreateProductRequest request);

    ProductDetailResponse update(Long id, UpdateProductRequest request);

    void delete(Long id);
}
