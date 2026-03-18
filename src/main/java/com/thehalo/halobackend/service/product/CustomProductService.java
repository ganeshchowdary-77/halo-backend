package com.thehalo.halobackend.service.product;

import com.thehalo.halobackend.dto.product.request.CustomProductRequest;
import com.thehalo.halobackend.dto.product.response.CustomProductResponse;
import com.thehalo.halobackend.dto.product.response.ChargeTypeOptionResponse;
import com.thehalo.halobackend.enums.ChargeType;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.exception.domain.product.ProductNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.model.product.CustomProduct;
import com.thehalo.halobackend.model.product.CustomCharge;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.CustomProductRepository;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom Product Service for handling products with custom charge types
 */
@Service
@RequiredArgsConstructor
public class CustomProductService {

    private final CustomProductRepository customProductRepository;
    private final ProductRepository productRepository;
    private final UserPlatformRepository platformRepository;

    /**
     * Create a custom product with selected charges
     */
    @Transactional
    public CustomProductResponse createCustomProduct(CustomProductRequest request) {
        // Validate base product
        Product baseProduct = productRepository.findById(request.getBaseProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getBaseProductId()));

        // Validate platform
        UserPlatform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new ProfileNotFoundException(request.getPlatformId()));

        // Create custom product
        CustomProduct customProduct = CustomProduct.builder()
                .baseProduct(baseProduct)
                .userPlatform(platform)
                .status(QuoteStatus.PENDING)
                .build();

        // Add custom charges
        List<CustomCharge> charges = request.getCustomCharges().stream()
                .map(chargeRequest -> CustomCharge.builder()
                        .customProduct(customProduct)
                        .chargeType(chargeRequest.getChargeType())
                        .chargeAmount(chargeRequest.getChargeAmount())
                        .description(chargeRequest.getDescription())
                        .build())
                .collect(Collectors.toList());

        customProduct.setCustomCharges(charges);

        // Calculate total premium
        BigDecimal totalCustomPremium = calculateTotalPremium(customProduct);
        customProduct.setTotalCustomPremium(totalCustomPremium);

        CustomProduct saved = customProductRepository.save(customProduct);
        return toCustomProductResponse(saved);
    }

    /**
     * Get available charge types with descriptions
     */
    public List<ChargeTypeOptionResponse> getAvailableChargeTypes() {
        return Arrays.asList(
            ChargeTypeOptionResponse.builder()
                .chargeType(ChargeType.LEGAL_CHARGES)
                .name("Legal Defense")
                .description("Legal defense and representation services")
                .billingType("Per incident")
                .estimatedCost(BigDecimal.valueOf(5000))
                .build(),
                
            ChargeTypeOptionResponse.builder()
                .chargeType(ChargeType.PR_CHARGES)
                .name("PR Crisis Management")
                .description("Public relations crisis management and reputation recovery")
                .billingType("Monthly retainer")
                .estimatedCost(BigDecimal.valueOf(2000))
                .build(),
                
            ChargeTypeOptionResponse.builder()
                .chargeType(ChargeType.MONITOR_CHARGES)
                .name("Reputation Monitoring")
                .description("24/7 reputation monitoring and alert system")
                .billingType("Monthly fee")
                .estimatedCost(BigDecimal.valueOf(500))
                .build(),
                
            ChargeTypeOptionResponse.builder()
                .chargeType(ChargeType.CRISIS_MANAGEMENT)
                .name("Emergency Response")
                .description("Emergency crisis response team activation")
                .billingType("Per activation")
                .estimatedCost(BigDecimal.valueOf(3000))
                .build(),
                
            ChargeTypeOptionResponse.builder()
                .chargeType(ChargeType.REPUTATION_RECOVERY)
                .name("Reputation Recovery")
                .description("Comprehensive reputation rehabilitation services")
                .billingType("Project-based")
                .estimatedCost(BigDecimal.valueOf(10000))
                .build(),
                
            ChargeTypeOptionResponse.builder()
                .chargeType(ChargeType.CONTENT_REMOVAL)
                .name("Content Removal")
                .description("Defamatory content removal and takedown services")
                .billingType("Per request")
                .estimatedCost(BigDecimal.valueOf(1000))
                .build(),
                
            ChargeTypeOptionResponse.builder()
                .chargeType(ChargeType.EXPERT_WITNESS)
                .name("Expert Witness")
                .description("Expert witness testimony and consultation")
                .billingType("Per case")
                .estimatedCost(BigDecimal.valueOf(7500))
                .build()
        );
    }

    /**
     * Calculate estimated premium for custom product
     */
    public BigDecimal calculateEstimatedPremium(CustomProductRequest request) {
        // Get base product
        Product baseProduct = productRepository.findById(request.getBaseProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getBaseProductId()));

        BigDecimal basePremium = baseProduct.getBasePremium();
        
        // Add custom charges
        BigDecimal customChargesTotal = request.getCustomCharges().stream()
                .map(charge -> charge.getChargeAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply platform risk multiplier (simplified for now)
        BigDecimal riskMultiplier = BigDecimal.valueOf(1.2); // Default multiplier for custom products
        
        return basePremium.multiply(riskMultiplier).add(customChargesTotal);
    }

    /**
     * Get custom products for underwriter review
     */
    public List<CustomProductResponse> getCustomProductsForReview() {
        return customProductRepository.findByStatus(QuoteStatus.PENDING)
                .stream()
                .map(this::toCustomProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approve custom product (underwriter action)
     */
    @Transactional
    public CustomProductResponse approveCustomProduct(Long customProductId, String underwriterNotes, 
                                                     BigDecimal finalPremium) {
        CustomProduct customProduct = customProductRepository.findById(customProductId)
                .orElseThrow(() -> new ProductNotFoundException(customProductId));

        customProduct.setStatus(QuoteStatus.APPROVED);
        customProduct.setUnderwriterNotes(underwriterNotes);
        customProduct.setTotalCustomPremium(finalPremium);

        CustomProduct saved = customProductRepository.save(customProduct);
        return toCustomProductResponse(saved);
    }

    private BigDecimal calculateTotalPremium(CustomProduct customProduct) {
        BigDecimal basePremium = customProduct.getBaseProduct().getBasePremium();
        
        BigDecimal customChargesTotal = customProduct.getCustomCharges().stream()
                .map(CustomCharge::getChargeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply risk multiplier based on platform
        BigDecimal riskMultiplier = getRiskMultiplier(customProduct.getUserPlatform());
        
        return basePremium.multiply(riskMultiplier).add(customChargesTotal);
    }

    private BigDecimal getRiskMultiplier(UserPlatform platform) {
        if (platform.getRiskLevel() != null) {
            return BigDecimal.valueOf(platform.getRiskLevel().getMultiplier());
        }
        return BigDecimal.valueOf(1.2); // Default for custom products
    }

    private CustomProductResponse toCustomProductResponse(CustomProduct customProduct) {
        return CustomProductResponse.builder()
                .id(customProduct.getId())
                .baseProductId(customProduct.getBaseProduct().getId())
                .baseProductName(customProduct.getBaseProduct().getName())
                .platformId(customProduct.getUserPlatform().getId())
                .platformHandle(customProduct.getUserPlatform().getHandle())
                .customCharges(customProduct.getCustomCharges().stream()
                        .map(charge -> CustomProductResponse.CustomChargeResponse.builder()
                                .chargeType(charge.getChargeType())
                                .chargeAmount(charge.getChargeAmount())
                                .description(charge.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .totalCustomPremium(customProduct.getTotalCustomPremium())
                .status(customProduct.getStatus())
                .underwriterNotes(customProduct.getUnderwriterNotes())
                .createdAt(customProduct.getCreatedAt())
                .build();
    }
}