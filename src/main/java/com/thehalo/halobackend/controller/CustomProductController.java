package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.product.request.CustomProductRequest;
import com.thehalo.halobackend.dto.product.response.CustomProductResponse;
import com.thehalo.halobackend.dto.product.response.ChargeTypeOptionResponse;
import com.thehalo.halobackend.service.product.CustomProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/custom-products")
@RequiredArgsConstructor
@Tag(name = "Custom Product Management", description = "Endpoints for managing custom insurance products")
public class CustomProductController {

    private final CustomProductService customProductService;

    @GetMapping("/charge-types")
    @Operation(summary = "Get available charge types", description = "Retrieves all available custom charge types with descriptions")
    @ApiResponse(responseCode = "200", description = "Charge types retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<ChargeTypeOptionResponse>>> getAvailableChargeTypes() {
        return ResponseFactory.success(customProductService.getAvailableChargeTypes(), "Charge types loaded");
    }

    @PostMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Create custom product", description = "Creates a new custom product with selected charges")
    @ApiResponse(responseCode = "201", description = "Custom product created successfully")
    public ResponseEntity<HaloApiResponse<CustomProductResponse>> createCustomProduct(
            @Valid @RequestBody CustomProductRequest request) {
        return ResponseFactory.success(
            customProductService.createCustomProduct(request), 
            "Custom product created", 
            HttpStatus.CREATED
        );
    }

    @PostMapping("/estimate")
    @PreAuthorize("hasRole('INFLUENCER')")
    @Operation(summary = "Calculate estimated premium", description = "Calculates estimated premium for custom product")
    @ApiResponse(responseCode = "200", description = "Premium calculated successfully")
    public ResponseEntity<HaloApiResponse<BigDecimal>> calculateEstimatedPremium(
            @Valid @RequestBody CustomProductRequest request) {
        return ResponseFactory.success(
            customProductService.calculateEstimatedPremium(request), 
            "Premium calculated"
        );
    }

    @GetMapping("/for-review")
    @PreAuthorize("hasRole('UNDERWRITER')")
    @Operation(summary = "Get custom products for review", description = "Retrieves custom products pending underwriter review")
    @ApiResponse(responseCode = "200", description = "Custom products retrieved successfully")
    public ResponseEntity<HaloApiResponse<List<CustomProductResponse>>> getCustomProductsForReview() {
        return ResponseFactory.success(
            customProductService.getCustomProductsForReview(), 
            "Custom products loaded"
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('UNDERWRITER')")
    @Operation(summary = "Approve custom product", description = "Approves a custom product with final premium")
    @ApiResponse(responseCode = "200", description = "Custom product approved successfully")
    public ResponseEntity<HaloApiResponse<CustomProductResponse>> approveCustomProduct(
            @PathVariable Long id,
            @RequestParam String underwriterNotes,
            @RequestParam BigDecimal finalPremium) {
        return ResponseFactory.success(
            customProductService.approveCustomProduct(id, underwriterNotes, finalPremium), 
            "Custom product approved"
        );
    }
}