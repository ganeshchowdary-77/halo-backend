package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.quote.request.QuotePricingRequest;
import com.thehalo.halobackend.dto.quote.response.QuotePricingResponse;
import com.thehalo.halobackend.service.quote.QuotePricingService;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.profile.UserProfile;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.UserProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Tag(name = "Quote Pricing", description = "Quote pricing and estimation")
public class QuotePricingController {

    private final QuotePricingService pricingService;
    private final ProductRepository productRepository;
    private final UserProfileRepository profileRepository;

    @PostMapping("/pricing")
    @Operation(summary = "Get quote pricing information")
    public ResponseEntity<?> getQuotePricing(@Valid @RequestBody QuotePricingRequest request) {
        
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        UserProfile profile = null;
        if (request.getInfluencerProfileId() != null) {
            profile = profileRepository.findById(request.getInfluencerProfileId())
                .orElse(null);
        }

        // Calculate base premium (starting from price)
        BigDecimal startingPrice = pricingService.calculateBasePremium(product, request.getPlatform());
        
        // Calculate personalized premium if profile provided
        BigDecimal personalizedPrice = null;
        boolean requiresUnderwriter = false;
        String riskLevel = "STANDARD";
        
        if (profile != null) {
            personalizedPrice = pricingService.calculatePersonalizedPremium(product, profile);
            requiresUnderwriter = pricingService.requiresUnderwriterReview(
                product, profile, request.getRequestedCoverage());
            riskLevel = determineRiskLevel(profile);
        }

        QuotePricingResponse response = QuotePricingResponse.builder()
            .startingFromPrice(startingPrice)
            .personalizedPrice(personalizedPrice)
            .displayText(String.format("Starting from $%.2f/month", startingPrice))
            .requiresProfile(profile == null)
            .requiresUnderwriter(requiresUnderwriter)
            .riskLevel(riskLevel)
            .platform(request.getPlatform().name())
            .productName(product.getName())
            .build();

        return ResponseFactory.success(response, "Pricing calculated successfully");
    }

    @GetMapping("/pricing/base/{productId}")
    @Operation(summary = "Get base pricing for a product across all platforms")
    public ResponseEntity<?> getBasePricing(@PathVariable Long productId) {
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        // Calculate base pricing for all platforms
        var platformPricing = java.util.Arrays.stream(com.thehalo.halobackend.enums.PlatformName.values())
            .collect(java.util.stream.Collectors.toMap(
                platform -> platform.name(),
                platform -> pricingService.calculateBasePremium(product, platform)
            ));

        return ResponseFactory.success(platformPricing, "Base pricing retrieved successfully");
    }

    @PostMapping("/estimate")
    @Operation(summary = "Quick pricing estimate without creating a quote")
    public ResponseEntity<?> getQuickEstimate(@Valid @RequestBody QuotePricingRequest request) {
        
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        BigDecimal estimate = pricingService.calculateBasePremium(product, request.getPlatform());
        
        // Apply basic adjustments based on coverage amount
        if (request.getRequestedCoverage() != null) {
            BigDecimal coverageMultiplier = request.getRequestedCoverage()
                .divide(BigDecimal.valueOf(100000), 2, java.math.RoundingMode.HALF_UP);
            estimate = estimate.multiply(coverageMultiplier);
        }

        var response = java.util.Map.of(
            "estimatedPremium", estimate,
            "platform", request.getPlatform().name(),
            "productName", product.getName(),
            "coverageAmount", request.getRequestedCoverage(),
            "disclaimer", "This is an estimate. Final pricing may vary based on your profile."
        );

        return ResponseFactory.success(response, "Estimate calculated successfully");
    }

    private String determineRiskLevel(UserProfile profile) {
        // Simple risk level determination
        if (profile.getNiche() == com.thehalo.halobackend.enums.Niche.FINANCE || 
            profile.getNiche() == com.thehalo.halobackend.enums.Niche.CRYPTO) {
            return "HIGH";
        }
        
        if (profile.getFollowerCount() > 1000000) {
            return "HIGH"; // High visibility = high risk
        }
        
        if (profile.getEngagementRate().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return "HIGH"; // Low engagement = potential fraud
        }
        
        return "STANDARD";
    }
}