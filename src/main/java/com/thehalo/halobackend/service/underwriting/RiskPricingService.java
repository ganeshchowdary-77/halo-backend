package com.thehalo.halobackend.service.underwriting;

import com.thehalo.halobackend.model.policy.PolicyApplication;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.model.underwriting.RiskParameter;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import com.thehalo.halobackend.enums.PlatformName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * RiskPricingService - Core actuarial engine for premium calculation and risk scoring.
 * Preserves the calculation logic refined for The Halo platform.
 */
@Service
@RequiredArgsConstructor
public class RiskPricingService {

    private final RiskParameterRepository riskParameterRepository;

    /**
     * Calculate base premium for "Starting from" display
     */
    public BigDecimal calculateBasePremium(Product product, PlatformName platform) {
        if (product == null || product.getBasePremium() == null) {
            throw new IllegalArgumentException("Product and its base premium must not be null");
        }
        if (platform == null) {
            throw new IllegalArgumentException("Platform name must not be null");
        }

        BigDecimal basePremium = product.getBasePremium();
        BigDecimal platformMultiplier = getPlatformBaseMultiplier(platform);
        BigDecimal calculatedPremium = basePremium.multiply(platformMultiplier);
        return calculatedPremium.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate personalized premium based on user profile
     */
    public BigDecimal calculatePersonalizedPremium(Product product, UserPlatform profile) {
        if (product == null || product.getBasePremium() == null) {
            throw new IllegalArgumentException("Product and its base premium must not be null");
        }
        if (profile == null) {
            throw new IllegalArgumentException("UserProfile must not be null");
        }

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

        if (finalPremium.compareTo(basePremium) < 0) {
            finalPremium = basePremium;
        }

        return finalPremium.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate personalized premium including security assessment from application
     */
    public BigDecimal calculatePersonalizedPremium(Product product, UserPlatform profile, PolicyApplication application) {
        BigDecimal basePersonalized = calculatePersonalizedPremium(product, profile);
        BigDecimal securityMultiplier = getSecurityMultiplier(application);
        BigDecimal finalPremium = basePersonalized.multiply(securityMultiplier).setScale(2, RoundingMode.HALF_UP);
        // Ensure premium never drops below the product base premium
        if (finalPremium.compareTo(product.getBasePremium()) < 0) {
            finalPremium = product.getBasePremium();
        }
        return finalPremium;
    }

    /**
     * Get security multiplier based on application security assessment
     */
    public BigDecimal getSecurityMultiplier(PolicyApplication application) {
        if (application == null) return BigDecimal.ONE;
        BigDecimal multiplier = BigDecimal.ONE;

        if (!Boolean.TRUE.equals(application.getHasTwoFactorAuth())) {
            multiplier = multiplier.multiply(BigDecimal.valueOf(1.3));
        }
        // 2FA enabled = no surcharge (baseline expected behavior)

        if ("NEVER".equalsIgnoreCase(application.getPasswordRotationFrequency())) {
            multiplier = multiplier.multiply(BigDecimal.valueOf(1.1));
        }

        if (Boolean.TRUE.equals(application.getThirdPartyManagement())) {
            multiplier = multiplier.multiply(BigDecimal.valueOf(1.2));
        }

        if ("FREQUENT".equalsIgnoreCase(application.getSponsoredContentFrequency())) {
            multiplier = multiplier.multiply(BigDecimal.valueOf(1.15));
        }

        return multiplier;
    }

    /**
     * Calculate risk score (0-100) for the profile
     */
    public int calculateRiskScore(UserPlatform profile, Product product) {
        return calculateRiskScore(profile, product, null);
    }

    public int calculateRiskScore(UserPlatform profile, Product product, PolicyApplication application) {
        if (profile == null || product == null) {
            throw new IllegalArgumentException("Profile and Product must not be null for risk calculation");
        }

        int baseScore = 50;

        // Follower count impact
        int followers = profile.getFollowerCount();
        if (followers < 1000) {
            baseScore += 20;
        } else if (followers < 10000) {
            baseScore += 10;
        } else if (followers > 1000000) {
            baseScore -= 10;
        } else if (followers > 100000) {
            baseScore -= 5;
        }

        // Engagement rate impact
        BigDecimal engagement = profile.getEngagementRate();
        if (engagement.compareTo(BigDecimal.valueOf(1.0)) < 0) {
            baseScore += 25;
        } else if (engagement.compareTo(BigDecimal.valueOf(2.0)) < 0) {
            baseScore += 15;
        } else if (engagement.compareTo(BigDecimal.valueOf(5.0)) > 0) {
            baseScore -= 10;
        }

        // Niche impact
        switch (profile.getNiche()) {
            case FINANCE:
            case CRYPTO:
                baseScore += 20;
                break;
            case POLITICS:
                baseScore += 15;
                break;
            case TECHNOLOGY:
            case EDUCATION:
            case FOOD:
                baseScore -= 10;
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
            baseScore += profile.getPreviousClaimsCount() * 8;
        }

        if (profile.getPreviousClaimsAmount() != null &&
            profile.getPreviousClaimsAmount().compareTo(BigDecimal.valueOf(100000)) > 0) {
            baseScore += 15;
        }

        // Coverage amount impact
        if (product.getCoverageAmount().compareTo(BigDecimal.valueOf(500000)) > 0) {
            baseScore += 10;
        }

        return Math.max(0, Math.min(100, baseScore));
    }

    /**
     * Check if application requires underwriter review based on risk factors
     */
    public boolean requiresUnderwriterReview(Product product, UserPlatform profile, PolicyApplication application) {
        int riskScore = calculateRiskScore(profile, product, application);

        if (riskScore > 70) return true;

        if (product.getCoverageAmount() != null &&
            product.getCoverageAmount().compareTo(BigDecimal.valueOf(500000)) > 0) {
            return true;
        }

        if (profile.getNiche() == com.thehalo.halobackend.enums.Niche.FINANCE ||
            profile.getNiche() == com.thehalo.halobackend.enums.Niche.CRYPTO ||
            profile.getNiche() == com.thehalo.halobackend.enums.Niche.POLITICS) {
            return true;
        }

        if (profile.getFollowerCount() > 5000000) return true;

        if (profile.getEngagementRate().compareTo(BigDecimal.valueOf(0.005)) < 0) return true;

        if (profile.getPreviousClaimsCount() != null && profile.getPreviousClaimsCount() >= 3) return true;

        return false;
    }

    public String getRiskLevel(int riskScore) {
        if (riskScore > 70) return "HIGH";
        if (riskScore < 40) return "LOW";
        return "MEDIUM";
    }

    // ── Multiplier methods (unchanged from legacy core) ──

    public BigDecimal getPlatformMultiplier(String platform) {
        return riskParameterRepository.findByParamKeyAndActiveTrue("PLATFORM_" + platform)
                .map(RiskParameter::getMultiplier)
                .orElse(BigDecimal.ONE);
    }

    public BigDecimal getFollowerMultiplier(int followers) {
        if (followers < 1000) {
            return riskParameterRepository.findByParamKeyAndActiveTrue("FOLLOWER_MICRO")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.valueOf(1.5));
        }
        if (followers < 10000) {
            return riskParameterRepository.findByParamKeyAndActiveTrue("FOLLOWER_MID")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.valueOf(1.2));
        }
        if (followers > 100000) {
            return riskParameterRepository.findByParamKeyAndActiveTrue("FOLLOWER_LARGE")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.ONE);
        }
        return riskParameterRepository.findByParamKeyAndActiveTrue("FOLLOWER_BASE")
            .map(RiskParameter::getMultiplier).orElse(BigDecimal.ONE);
    }

    public BigDecimal getEngagementMultiplier(BigDecimal engagement) {
        if (engagement == null) return BigDecimal.ONE;

        if (engagement.compareTo(BigDecimal.valueOf(0.02)) < 0) {
            return riskParameterRepository.findByParamKeyAndActiveTrue("ENGAGEMENT_LOW")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.valueOf(1.3));
        }
        if (engagement.compareTo(BigDecimal.valueOf(0.05)) > 0) {
            return riskParameterRepository.findByParamKeyAndActiveTrue("ENGAGEMENT_HIGH")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.ONE);
        }
        return riskParameterRepository.findByParamKeyAndActiveTrue("ENGAGEMENT_BASE")
            .map(RiskParameter::getMultiplier).orElse(BigDecimal.ONE);
    }

    public BigDecimal getNicheMultiplier(String niche) {
        return riskParameterRepository.findByParamKeyAndActiveTrue("NICHE_" + niche)
                .map(RiskParameter::getMultiplier)
                .orElse(BigDecimal.ONE);
    }

    public BigDecimal getCoverageMultiplier(BigDecimal coverage, int followers) {
        BigDecimal multiplier = BigDecimal.ONE;

        if (followers > 1000000) {
            multiplier = riskParameterRepository.findByParamKeyAndActiveTrue("COVERAGE_MEGA")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.valueOf(1.2));
        } else if (followers > 100000) {
            multiplier = riskParameterRepository.findByParamKeyAndActiveTrue("COVERAGE_LARGE")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.valueOf(1.1));
        }

        if (coverage != null && coverage.compareTo(BigDecimal.valueOf(100000)) > 0) {
            BigDecimal highCoverageMult = riskParameterRepository.findByParamKeyAndActiveTrue("COVERAGE_HIGH_LIMIT")
                .map(RiskParameter::getMultiplier).orElse(BigDecimal.valueOf(1.1));
            multiplier = multiplier.multiply(highCoverageMult);
        }
        return multiplier;
    }

    private BigDecimal getPlatformBaseMultiplier(PlatformName platform) {
        String platformKey = "PLATFORM_BASE_" + platform.name();
        return riskParameterRepository.findByParamKeyAndActiveTrue(platformKey)
            .map(RiskParameter::getMultiplier)
            .orElse(BigDecimal.valueOf(1.0));
    }
}
