package com.thehalo.halobackend.service.quote;

import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.model.underwriting.RiskParameter;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import com.thehalo.halobackend.enums.PlatformName;
import com.thehalo.halobackend.enums.Niche;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.thehalo.halobackend.dto.quote.response.QuotePricingResponse;
import com.thehalo.halobackend.dto.underwriter.response.PremiumCalculationLogResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
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
        return calculatedPremium.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate personalized premium based on user profile
     * Uses multiplicative adjustments standard in insurance
     * Ensures final premium is always >= base premium
     */
    public BigDecimal calculatePersonalizedPremium(Product product, UserPlatform profile) {
        BigDecimal basePremium = product.getBasePremium();
        
        BigDecimal platformMultiplier = getPlatformMultiplier(profile.getPlatform() != null ? profile.getPlatform().getName().name() : "LACKING");
        BigDecimal followerMultiplier = getFollowerMultiplier(profile.getFollowerCount());
        BigDecimal engagementMultiplier = getEngagementMultiplier(profile.getEngagementRate());
        BigDecimal nicheMultiplier = getNicheMultiplier(profile.getNiche() != null ? profile.getNiche().name() : "LACKING");
        BigDecimal coverageMultiplier = getCoverageMultiplier(product.getCoverageAmount(), profile.getFollowerCount());
        
        BigDecimal finalPremium = basePremium
            .multiply(platformMultiplier)
            .multiply(followerMultiplier)
            .multiply(engagementMultiplier)
            .multiply(nicheMultiplier)
            .multiply(coverageMultiplier);
        
        // Ensure final premium is never below base premium
        if (finalPremium.compareTo(basePremium) < 0) {
            finalPremium = basePremium;
        }
            
        return finalPremium.setScale(2, RoundingMode.HALF_UP);
    }
    
    // Multiplier getters reading from risk_parameters table with fallbacks
    
    public BigDecimal getPlatformMultiplier(String platform) {
        return riskParameterRepository.findByParamKeyAndActiveTrue("PLATFORM_" + platform)
                .map(RiskParameter::getMultiplier)
                .orElse(BigDecimal.ONE);
    }
    
    public BigDecimal getFollowerMultiplier(int followers) {
        return riskParameterRepository.findByParamKeyAndActiveTrue("FOLLOWER_MULT")
            .map(RiskParameter::getMultiplier)
            .orElseGet(() -> {
                if (followers < 1000) return BigDecimal.valueOf(1.5);
                if (followers < 10000) return BigDecimal.valueOf(1.2);
                if (followers > 100000) return BigDecimal.valueOf(0.8);
                return BigDecimal.ONE;
            });
    }
    
    public BigDecimal getEngagementMultiplier(BigDecimal engagement) {
        if (engagement == null) return BigDecimal.ONE;
        return riskParameterRepository.findByParamKeyAndActiveTrue("ENGAGEMENT_MULT")
            .map(RiskParameter::getMultiplier)
            .orElseGet(() -> {
                if (engagement.compareTo(BigDecimal.valueOf(0.02)) < 0) return BigDecimal.valueOf(1.3);
                if (engagement.compareTo(BigDecimal.valueOf(0.05)) > 0) return BigDecimal.valueOf(0.9);
                return BigDecimal.ONE;
            });
    }
    
    public BigDecimal getNicheMultiplier(String niche) {
        return riskParameterRepository.findByParamKeyAndActiveTrue("NICHE_" + niche)
                .map(RiskParameter::getMultiplier)
                .orElse(BigDecimal.ONE);
    }
    
    public BigDecimal getCoverageMultiplier(BigDecimal coverage, int followers) {
        return riskParameterRepository.findByParamKeyAndActiveTrue("COVERAGE_MULT")
            .map(RiskParameter::getMultiplier)
            .orElseGet(() -> {
                BigDecimal multiplier = BigDecimal.ONE;
                if (followers > 1000000) multiplier = BigDecimal.valueOf(1.2);
                else if (followers > 100000) multiplier = BigDecimal.valueOf(1.1);
                
                if (coverage != null && coverage.compareTo(BigDecimal.valueOf(100000)) > 0) {
                    multiplier = multiplier.multiply(BigDecimal.valueOf(1.1));
                }
                return multiplier;
            });
    }

    /**
     * Base platform multipliers for "Starting from" pricing
     */
    private BigDecimal getPlatformBaseMultiplier(PlatformName platform) {
        String platformKey = "PLATFORM_BASE_" + platform.name();
        return riskParameterRepository.findByParamKeyAndActiveTrue(platformKey)
            .map(RiskParameter::getMultiplier)
            .orElse(BigDecimal.valueOf(1.0)); // Default base multiplier should be 1.0
    }

    /**
     * Check if quote requires underwriter review based on risk score
     */
    public boolean requiresUnderwriterReview(Product product, UserPlatform profile, BigDecimal requestedCoverage) {
        // Calculate risk score (0-100)
        int riskScore = calculateRiskScore(profile, product);
        
        // Risk score threshold - if > 70, requires review
        if (riskScore > 70) {
            return true;
        }
        
        // High coverage amounts
        if (requestedCoverage.compareTo(BigDecimal.valueOf(500000)) > 0) {
            return true;
        }
        
        // High-risk niches
        if (profile.getNiche() == com.thehalo.halobackend.enums.Niche.FINANCE || 
            profile.getNiche() == com.thehalo.halobackend.enums.Niche.CRYPTO ||
            profile.getNiche() == com.thehalo.halobackend.enums.Niche.POLITICS) {
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
        
        // Multiple previous claims
        if (profile.getPreviousClaimsCount() != null && profile.getPreviousClaimsCount() >= 3) {
            return true;
        }
        
        return false;
    }

    /**
     * Calculate risk score (0-100) for the profile
     */
    public int calculateRiskScore(UserPlatform profile, Product product) {
        int baseScore = 50; // Start with medium risk
        
        // Follower count impact
        int followers = profile.getFollowerCount();
        if (followers < 1000) {
            baseScore += 20; // Higher risk
        } else if (followers < 10000) {
            baseScore += 10;
        } else if (followers > 1000000) {
            baseScore -= 10; // Lower risk
        } else if (followers > 100000) {
            baseScore -= 5;
        }
        
        // Engagement rate impact (stored as percentage: 5.0 = 5%)
        BigDecimal engagement = profile.getEngagementRate();
        if (engagement.compareTo(BigDecimal.valueOf(1.0)) < 0) { // < 1%
            baseScore += 25; // Very suspicious
        } else if (engagement.compareTo(BigDecimal.valueOf(2.0)) < 0) { // < 2%
            baseScore += 15;
        } else if (engagement.compareTo(BigDecimal.valueOf(5.0)) > 0) { // > 5%
            baseScore -= 10; // Good engagement
        }
        
        // Niche impact
        switch (profile.getNiche()) {
            case FINANCE:
            case CRYPTO:
                baseScore += 20; // High risk
                break;
            case POLITICS:
                baseScore += 15;
                break;
            case TECHNOLOGY:
            case EDUCATION:
            case FOOD:
                baseScore -= 10; // Low risk
                break;
            case ENTERTAINMENT:
            case COMEDY:
            case LIFESTYLE:
            case BEAUTY:
            case FASHION:
            case FITNESS:
            case TRAVEL:
            case MUSIC:
                baseScore -= 5;
                break;
            case OTHER:
                baseScore += 10;
                break;
            default:
                break;
        }
        
        // Previous claims impact
        if (profile.getPreviousClaimsCount() != null) {
            baseScore += profile.getPreviousClaimsCount() * 8; // Each claim adds risk
        }
        
        if (profile.getPreviousClaimsAmount() != null && 
            profile.getPreviousClaimsAmount().compareTo(BigDecimal.valueOf(100000)) > 0) {
            baseScore += 15; // High previous claim amounts
        }
        
        // Coverage amount impact
        if (product.getCoverageAmount().compareTo(BigDecimal.valueOf(500000)) > 0) {
            baseScore += 10;
        }
        
        // Ensure score is within bounds
        return Math.max(0, Math.min(100, baseScore));
    }

    private String calculateRiskLevel(UserPlatform profile, Product product) {
        int riskScore = calculateRiskScore(profile, product);
        if (riskScore > 70) {
            return "HIGH";
        } else if (riskScore < 40) {
            return "LOW";
        }
        return "MEDIUM";
    }

    /**
     * Get pricing display information
     */
    public QuotePricingResponse getPricingDisplay(Product product, PlatformName platform, UserPlatform profile) {
        BigDecimal basePremium = calculateBasePremium(product, platform);
        
        return QuotePricingResponse.builder()
            .startingFromPrice(basePremium)
            .displayText(String.format("Starting from $%.2f/month", basePremium))
            .requiresProfile(profile == null)
            .personalizedPrice(profile != null ? calculatePersonalizedPremium(product, profile) : null)
            .requiresUnderwriter(profile != null ? requiresUnderwriterReview(product, profile, product.getCoverageAmount()) : false)
            .riskLevel(profile != null ? calculateRiskLevel(profile, product) : "STANDARD")
            .platform(platform.name())
            .productName(product.getName())
            .build();
    }
    
    public PremiumCalculationLogResponse buildCalculationLog(QuoteRequest quote) {
        if (quote.getProduct() == null || quote.getProfile() == null) {
            return PremiumCalculationLogResponse.builder()
                    .quoteId(quote.getId())
                    .quoteNumber(quote.getQuoteNumber())
                    .basePremium(BigDecimal.ZERO)
                    .finalPremium(BigDecimal.ZERO)
                    .calculationSteps(java.util.List.of("Unable to calculate - missing product or profile data"))
                    .build();
        }
        
        BigDecimal basePremium = quote.getProduct().getBasePremium();
        
        BigDecimal platformMultiplier = getPlatformMultiplier(quote.getProfile().getPlatform() != null ? quote.getProfile().getPlatform().getName().name() : "LACKING");
        BigDecimal followerMultiplier = getFollowerMultiplier(quote.getProfile().getFollowerCount());
        BigDecimal engagementMultiplier = getEngagementMultiplier(quote.getProfile().getEngagementRate());
        BigDecimal nicheMultiplier = getNicheMultiplier(quote.getProfile().getNiche() != null ? quote.getProfile().getNiche().name() : "LACKING");
        BigDecimal coverageMultiplier = getCoverageMultiplier(quote.getProduct().getCoverageAmount(), quote.getProfile().getFollowerCount());
        
        BigDecimal afterPlatform = basePremium.multiply(platformMultiplier);
        BigDecimal afterFollower = afterPlatform.multiply(followerMultiplier);
        BigDecimal afterEngagement = afterFollower.multiply(engagementMultiplier);
        BigDecimal afterNiche = afterEngagement.multiply(nicheMultiplier);
        BigDecimal finalPremium = afterNiche.multiply(coverageMultiplier);
        
        // Calculate risk score
        int riskScore = calculateRiskScore(quote.getProfile(), quote.getProduct());
        String riskLevel = calculateRiskLevel(quote.getProfile(), quote.getProduct());
        
        // Get audit information
        String assignedUnderwriterName = quote.getAssignedUnderwriter() != null 
            ? quote.getAssignedUnderwriter().getFullName() 
            : null;
        
        String reviewedBy = null;
        String reviewedAt = null;
        
        // If quote is approved or rejected, show who did it
        if (quote.getStatus() == com.thehalo.halobackend.enums.QuoteStatus.APPROVED || 
            quote.getStatus() == com.thehalo.halobackend.enums.QuoteStatus.REJECTED) {
            if (quote.getAssignedUnderwriter() != null) {
                reviewedBy = quote.getAssignedUnderwriter().getFullName();
            } else {
                reviewedBy = "System (Auto-approved)";
            }
            reviewedAt = quote.getReviewedAt() != null 
                ? quote.getReviewedAt().toString() 
                : quote.getUpdatedAt().toString();
        }
        
        return PremiumCalculationLogResponse.builder()
                .quoteId(quote.getId())
                .quoteNumber(quote.getQuoteNumber())
                .basePremium(basePremium)
                .platformMultiplier(platformMultiplier)
                .platformName(quote.getProfile().getPlatform() != null ? quote.getProfile().getPlatform().getName().name() : "Unknown")
                .afterPlatform(afterPlatform)
                .followerMultiplier(followerMultiplier)
                .followerCount(quote.getProfile().getFollowerCount())
                .afterFollower(afterFollower)
                .engagementMultiplier(engagementMultiplier)
                .engagementRate(quote.getProfile().getEngagementRate() != null ? quote.getProfile().getEngagementRate() : BigDecimal.ZERO)
                .afterEngagement(afterEngagement)
                .nicheMultiplier(nicheMultiplier)
                .niche(quote.getProfile().getNiche() != null ? quote.getProfile().getNiche().name() : "Unknown")
                .afterNiche(afterNiche)
                .coverageMultiplier(coverageMultiplier)
                .coverageAmount(quote.getProduct().getCoverageAmount() != null ? quote.getProduct().getCoverageAmount() : BigDecimal.ZERO)
                .finalPremium(finalPremium)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .assignedUnderwriterName(assignedUnderwriterName)
                .reviewedBy(reviewedBy)
                .reviewedAt(reviewedAt)
                .calculationSteps(java.util.List.of(
                        String.format("1. Base Premium: $%.2f", basePremium),
                        String.format("2. Platform Adjustment (×%.2f): $%.2f", platformMultiplier, afterPlatform),
                        String.format("3. Follower Count Adjustment (×%.2f): $%.2f", followerMultiplier, afterFollower),
                        String.format("4. Engagement Rate Adjustment (×%.2f): $%.2f", engagementMultiplier, afterEngagement),
                        String.format("5. Niche Risk Adjustment (×%.2f): $%.2f", nicheMultiplier, afterNiche),
                        String.format("6. Coverage Amount Adjustment (×%.2f): $%.2f", coverageMultiplier, finalPremium),
                        String.format("FINAL PREMIUM: $%.2f/month", finalPremium)
                ))
                .build();
    }
}