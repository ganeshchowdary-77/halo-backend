package com.thehalo.halobackend.service.quote;

import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.profile.UserProfile;
import com.thehalo.halobackend.model.RiskParameter;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import com.thehalo.halobackend.enums.PlatformName;
import com.thehalo.halobackend.enums.Niche;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotePricingService {

    private final RiskParameterRepository riskParameterRepository;

    /**
     * Calculate base premium for "Starting from" display
     * Uses minimum risk parameters for the platform
     */
    public BigDecimal calculateBasePremium(Product product, PlatformName platform) {
        BigDecimal basePremium = product.getBasePremium();
        
        // Platform-specific base multipliers
        BigDecimal platformMultiplier = getPlatformBaseMultiplier(platform);
        
        BigDecimal calculatedPremium = basePremium.multiply(platformMultiplier);
        
        log.debug("Base premium for product {} on platform {}: {}", 
            product.getName(), platform, calculatedPremium);
            
        return calculatedPremium.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate personalized premium based on user profile
     * This is the actual premium they'll pay
     */
    public BigDecimal calculatePersonalizedPremium(Product product, UserProfile profile) {
        BigDecimal basePremium = product.getBasePremium();
        
        // Risk factors calculation
        BigDecimal riskMultiplier = calculateRiskMultiplier(profile);
        
        // Platform-specific adjustments
        BigDecimal platformMultiplier = getPlatformMultiplier(profile);
        
        // Coverage amount adjustment
        BigDecimal coverageMultiplier = calculateCoverageMultiplier(
            product.getBasePremium(), profile.getFollowerCount());
        
        BigDecimal finalPremium = basePremium
            .multiply(riskMultiplier)
            .multiply(platformMultiplier)
            .multiply(coverageMultiplier);
            
        log.info("Calculated personalized premium for {} on {}: {} (base: {}, risk: {}, platform: {}, coverage: {})",
            profile.getUser().getEmail(), profile.getPlatform().getName(), finalPremium,
            basePremium, riskMultiplier, platformMultiplier, coverageMultiplier);
            
        return finalPremium.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate risk multiplier based on user profile
     */
    private BigDecimal calculateRiskMultiplier(UserProfile profile) {
        BigDecimal riskScore = BigDecimal.ONE;
        
        // Follower count risk (higher followers = lower risk per follower)
        int followers = profile.getFollowerCount();
        if (followers < 1000) {
            riskScore = riskScore.multiply(BigDecimal.valueOf(1.5)); // Higher risk
        } else if (followers < 10000) {
            riskScore = riskScore.multiply(BigDecimal.valueOf(1.2));
        } else if (followers > 100000) {
            riskScore = riskScore.multiply(BigDecimal.valueOf(0.8)); // Lower risk
        }
        
        // Engagement rate risk
        BigDecimal engagementRate = profile.getEngagementRate();
        if (engagementRate.compareTo(BigDecimal.valueOf(0.02)) < 0) { // < 2%
            riskScore = riskScore.multiply(BigDecimal.valueOf(1.3)); // Low engagement = higher risk
        } else if (engagementRate.compareTo(BigDecimal.valueOf(0.05)) > 0) { // > 5%
            riskScore = riskScore.multiply(BigDecimal.valueOf(0.9)); // High engagement = lower risk
        }
        
        // Niche-specific risk
        switch (profile.getNiche()) {
            case FINANCE:
            case CRYPTO:
                riskScore = riskScore.multiply(BigDecimal.valueOf(1.4)); // Higher risk niches
                break;
            case LIFESTYLE:
            case FOOD:
                riskScore = riskScore.multiply(BigDecimal.valueOf(0.9)); // Lower risk niches
                break;
            default:
                // No adjustment for other niches
                break;
        }
        
        return riskScore;
    }

    /**
     * Platform-specific multipliers for personalized quotes
     */
    private BigDecimal getPlatformMultiplier(UserProfile profile) {
        String platformKey = "PLATFORM_" + profile.getPlatform().getName().name();
        return riskParameterRepository.findByParamKeyAndActiveTrue(platformKey)
            .map(RiskParameter::getMultiplier)
            .orElse(BigDecimal.valueOf(1.0)); // Default multiplier if not found
    }

    /**
     * Base platform multipliers for "Starting from" pricing
     */
    private BigDecimal getPlatformBaseMultiplier(PlatformName platform) {
        String platformKey = "PLATFORM_BASE_" + platform.name();
        return riskParameterRepository.findByParamKeyAndActiveTrue(platformKey)
            .map(RiskParameter::getMultiplier)
            .orElse(BigDecimal.valueOf(0.8)); // Default base multiplier if not found
    }

    /**
     * Coverage amount adjustment based on follower count
     */
    private BigDecimal calculateCoverageMultiplier(BigDecimal coverageAmount, int followerCount) {
        // Higher coverage for larger influencers
        BigDecimal baseMultiplier = BigDecimal.ONE;
        
        if (followerCount > 1000000) { // 1M+ followers
            baseMultiplier = BigDecimal.valueOf(1.2);
        } else if (followerCount > 100000) { // 100K+ followers
            baseMultiplier = BigDecimal.valueOf(1.1);
        }
        
        // Adjust based on coverage amount
        if (coverageAmount.compareTo(BigDecimal.valueOf(100000)) > 0) { // > $100K coverage
            baseMultiplier = baseMultiplier.multiply(BigDecimal.valueOf(1.1));
        }
        
        return baseMultiplier;
    }

    /**
     * Check if quote requires underwriter review
     */
    public boolean requiresUnderwriterReview(Product product, UserProfile profile, BigDecimal requestedCoverage) {
        // High coverage amounts
        if (requestedCoverage.compareTo(BigDecimal.valueOf(500000)) > 0) {
            return true;
        }
        
        // High-risk niches
        if (profile.getNiche() == com.thehalo.halobackend.enums.Niche.FINANCE || 
            profile.getNiche() == com.thehalo.halobackend.enums.Niche.CRYPTO) {
            return true;
        }
        
        // Very large influencers (custom pricing)
        if (profile.getFollowerCount() > 5000000) {
            return true;
        }
        
        // Low engagement rate (potential fraud)
        if (profile.getEngagementRate().compareTo(BigDecimal.valueOf(0.005)) < 0) { // < 0.5%
            return true;
        }
        
        return false;
    }

    /**
     * Get pricing display information
     */
    public Map<String, Object> getPricingDisplay(Product product, PlatformName platform, UserProfile profile) {
        BigDecimal basePremium = calculateBasePremium(product, platform);
        
        Map<String, Object> pricing = Map.of(
            "startingFrom", basePremium,
            "displayText", String.format("Starting from $%.2f/month", basePremium),
            "requiresProfile", profile == null,
            "personalizedPremium", profile != null ? calculatePersonalizedPremium(product, profile) : null,
            "requiresUnderwriter", profile != null ? requiresUnderwriterReview(product, profile, product.getCoverageAmount()) : false
        );
        
        return pricing;
    }
}