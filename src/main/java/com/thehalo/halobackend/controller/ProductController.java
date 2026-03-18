package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.product.request.CreateProductRequest;
import com.thehalo.halobackend.dto.product.request.UpdateProductRequest;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.dto.product.response.PublicProductResponse;
import com.thehalo.halobackend.service.product.ProductService;
import com.thehalo.halobackend.service.product.PublicProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Product (insurance plan) management endpoints
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing and viewing insurance products")
public class ProductController {

    private final ProductService productService;
    private final PublicProductService publicProductService;

    // Public: landing page product listing — no auth required
    @GetMapping("/public")
    @Operation(summary = "Get public products for landing page", description = "Returns all active products for public display. No authentication required.")
    @ApiResponse(responseCode = "200", description = "List of products retrieved")
    public ResponseEntity<com.thehalo.halobackend.dto.common.HaloApiResponse<List<PublicProductResponse>>> getPublicProducts() {
        return ResponseFactory.success(publicProductService.getPublicProducts(), "Products loaded");
    }

    // Public: product detail for landing page — no auth required
    @GetMapping("/public/{id}")
    @Operation(summary = "Get public product detail", description = "Returns detailed product information for public view. No authentication required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details retrieved"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<com.thehalo.halobackend.dto.common.HaloApiResponse<ProductDetailResponse>> getPublicProductDetail(@PathVariable Long id) {
        return ResponseFactory.success(publicProductService.getPublicProductDetail(id), "Product detail loaded");
    }

    // Authenticated: full product summary list
    @GetMapping
    @Operation(summary = "Get active products", description = "Returns a list of all active insurance products. Requires authentication.")
    @ApiResponse(responseCode = "200", description = "List of products retrieved")
    public ResponseEntity<com.thehalo.halobackend.dto.common.HaloApiResponse<List<ProductSummaryResponse>>> getActiveSummaries() {
        return ResponseFactory.success(productService.getActiveSummaries(), "Products loaded");
    }

    // Authenticated: full product detail view
    @GetMapping("/{id}")
    @Operation(summary = "Get product details", description = "Returns full details for a specific product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<com.thehalo.halobackend.dto.common.HaloApiResponse<ProductDetailResponse>> getDetail(
            @PathVariable Long id) {
        return ResponseFactory.success(productService.getDetail(id), "Product loaded");
    }

    // Admin only: create a new insurance plan
    @PostMapping
    @PreAuthorize("hasRole('POLICY_ADMIN')")
    @Operation(summary = "Create a new product", description = "Creates a new insurance product. Requires POLICY_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires POLICY_ADMIN role"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate name")
    })
    public ResponseEntity<com.thehalo.halobackend.dto.common.HaloApiResponse<ProductDetailResponse>> create(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseFactory.success(productService.create(request), "Product created", HttpStatus.CREATED);
    }

    // Admin only: partially update an existing plan
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('POLICY_ADMIN')")
    @Operation(summary = "Update an existing product", description = "Updates an existing product. Requires POLICY_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires POLICY_ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<com.thehalo.halobackend.dto.common.HaloApiResponse<ProductDetailResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseFactory.success(productService.update(id, request), "Product updated");
    }

    // Admin only: soft-delete a plan
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('POLICY_ADMIN')")
    @Operation(summary = "Soft delete a product", description = "Marks a product as deleted. Requires POLICY_ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product successfully deactivated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires POLICY_ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<com.thehalo.halobackend.dto.common.HaloApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseFactory.success("Product deactivated");
    }
}