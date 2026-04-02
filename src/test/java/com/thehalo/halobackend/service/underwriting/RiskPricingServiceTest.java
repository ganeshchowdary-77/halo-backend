package com.thehalo.halobackend.service.underwriting;

import com.thehalo.halobackend.enums.Niche;
import com.thehalo.halobackend.enums.PlatformName;
import com.thehalo.halobackend.model.platform.Platform;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.underwriting.RiskParameter;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RiskPricingServiceTest {

    @Mock
    private RiskParameterRepository riskParameterRepository;

    @InjectMocks
    private RiskPricingService riskPricingService;

    private Product testProduct;
    private Platform instagramPlatform;
    private Platform twitterPlatform;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .basePremium(BigDecimal.valueOf(100.00))
                .coverageLimitLegal(BigDecimal.valueOf(50000))
                .coverageLimitReputation(BigDecimal.valueOf(30000))
                .coverageLimitCyber(BigDecimal.valueOf(20000))
                .build();

        instagramPlatform = new Platform(1L, PlatformName.INSTAGRAM, 1.0, "Instagram");
        twitterPlatform = new Platform(2L, PlatformName.X, 1.1, "X");

        // Mock risk parameters
        when(riskParameterRepository.findByParamKeyAndActiveTrue("PLATFORM_INSTAGRAM"))
                .thenReturn(Optional.of(RiskParameter.builder()
                        .paramKey("PLATFORM_INSTAGRAM")
                        .multiplier(BigDecimal.valueOf(1.05)) // +5%
                        .build()));

        when(riskParameterRepository.findByParamKeyAndActiveTrue("PLATFORM_TWITTER"))
                .thenReturn(Optional.of(RiskParameter.builder()
                        .paramKey("PLATFORM_TWITTER")
                        .multiplier(BigDecimal.valueOf(1.15)) // +15%
                        .build()));
    }

    @Test
    void calculatePersonalizedPremium_ShouldAlwaysBeGreaterThanOrEqualToBase_SmallInfluencer() {
        // Small influencer with good engagement
        UserPlatform profile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(5000)
                .engagementRate(BigDecimal.valueOf(3.5))
                .niche(Niche.LIFESTYLE)
                .build();

        BigDecimal premium = riskPricingService.calculatePersonalizedPremium(testProduct, profile);

        assertThat(premium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        System.out.println("Small Influencer Premium: $" + premium + " (Base: $" + testProduct.getBasePremium() + ")");
    }

    @Test
    void calculatePersonalizedPremium_ShouldAlwaysBeGreaterThanOrEqualToBase_LargeInfluencer() {
        // Large influencer with excellent engagement
        UserPlatform profile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(2000000)
                .engagementRate(BigDecimal.valueOf(6.0))
                .niche(Niche.LIFESTYLE)
                .build();

        BigDecimal premium = riskPricingService.calculatePersonalizedPremium(testProduct, profile);

        assertThat(premium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        System.out.println("Large Influencer Premium: $" + premium + " (Base: $" + testProduct.getBasePremium() + ")");
    }

    @Test
    void calculatePersonalizedPremium_ShouldBeHigher_ForHighRiskNiche() {
        // Crypto influencer (high risk)
        UserPlatform cryptoProfile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.CRYPTO)
                .build();

        // Lifestyle influencer (low risk)
        UserPlatform lifestyleProfile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.LIFESTYLE)
                .build();

        BigDecimal cryptoPremium = riskPricingService.calculatePersonalizedPremium(testProduct, cryptoProfile);
        BigDecimal lifestylePremium = riskPricingService.calculatePersonalizedPremium(testProduct, lifestyleProfile);

        assertThat(cryptoPremium).isGreaterThan(lifestylePremium);
        assertThat(cryptoPremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        assertThat(lifestylePremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        
        System.out.println("Crypto Premium: $" + cryptoPremium);
        System.out.println("Lifestyle Premium: $" + lifestylePremium);
    }

    @Test
    void calculatePersonalizedPremium_ShouldBeHigher_ForLowEngagement() {
        // Low engagement (suspicious)
        UserPlatform lowEngagement = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(0.5)) // 0.5%
                .niche(Niche.LIFESTYLE)
                .build();

        // Good engagement
        UserPlatform goodEngagement = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(4.0)) // 4%
                .niche(Niche.LIFESTYLE)
                .build();

        BigDecimal lowEngagementPremium = riskPricingService.calculatePersonalizedPremium(testProduct, lowEngagement);
        BigDecimal goodEngagementPremium = riskPricingService.calculatePersonalizedPremium(testProduct, goodEngagement);

        assertThat(lowEngagementPremium).isGreaterThan(goodEngagementPremium);
        assertThat(lowEngagementPremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        assertThat(goodEngagementPremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        
        System.out.println("Low Engagement Premium: $" + lowEngagementPremium);
        System.out.println("Good Engagement Premium: $" + goodEngagementPremium);
    }

    @Test
    void calculatePersonalizedPremium_ShouldBeHigher_WithPreviousClaims() {
        // No previous claims
        UserPlatform noClaims = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.LIFESTYLE)
                .previousClaimsCount(0)
                .build();

        // With previous claims
        UserPlatform withClaims = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.LIFESTYLE)
                .previousClaimsCount(2)
                .previousClaimsAmount(BigDecimal.valueOf(60000))
                .build();

        BigDecimal noClaimsPremium = riskPricingService.calculatePersonalizedPremium(testProduct, noClaims);
        BigDecimal withClaimsPremium = riskPricingService.calculatePersonalizedPremium(testProduct, withClaims);

        assertThat(withClaimsPremium).isGreaterThan(noClaimsPremium);
        assertThat(withClaimsPremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        assertThat(noClaimsPremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        
        System.out.println("No Claims Premium: $" + noClaimsPremium);
        System.out.println("With Claims Premium: $" + withClaimsPremium);
    }

    @Test
    void calculatePersonalizedPremium_ShouldBeHigher_ForMicroInfluencer() {
        // Micro influencer (< 1000 followers)
        UserPlatform microInfluencer = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(500)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.LIFESTYLE)
                .build();

        // Medium influencer
        UserPlatform mediumInfluencer = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.LIFESTYLE)
                .build();

        BigDecimal microPremium = riskPricingService.calculatePersonalizedPremium(testProduct, microInfluencer);
        BigDecimal mediumPremium = riskPricingService.calculatePersonalizedPremium(testProduct, mediumInfluencer);

        assertThat(microPremium).isGreaterThan(mediumPremium);
        assertThat(microPremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        assertThat(mediumPremium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        
        System.out.println("Micro Influencer Premium: $" + microPremium);
        System.out.println("Medium Influencer Premium: $" + mediumPremium);
    }

    @Test
    void calculatePersonalizedPremium_ShouldNeverBeLessThanBase_WorstCase() {
        // Worst case scenario: all factors that could reduce premium
        UserPlatform worstCase = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(10000000) // Very large
                .engagementRate(BigDecimal.valueOf(8.0)) // Excellent engagement
                .niche(Niche.EDUCATION) // Low risk niche
                .previousClaimsCount(0)
                .build();

        BigDecimal premium = riskPricingService.calculatePersonalizedPremium(testProduct, worstCase);

        assertThat(premium).isGreaterThanOrEqualTo(testProduct.getBasePremium());
        System.out.println("Best Case Premium: $" + premium + " (Base: $" + testProduct.getBasePremium() + ")");
    }

    @Test
    void calculatePersonalizedPremium_ShouldBeSignificantlyHigher_WorstRiskProfile() {
        // Worst risk profile
        UserPlatform worstRisk = UserPlatform.builder()
                .platform(twitterPlatform)
                .followerCount(500) // Very small
                .engagementRate(BigDecimal.valueOf(0.3)) // Very low
                .niche(Niche.CRYPTO) // High risk
                .previousClaimsCount(3)
                .previousClaimsAmount(BigDecimal.valueOf(100000))
                .build();

        BigDecimal premium = riskPricingService.calculatePersonalizedPremium(testProduct, worstRisk);

        assertThat(premium).isGreaterThan(testProduct.getBasePremium().multiply(BigDecimal.valueOf(1.5)));
        System.out.println("Worst Risk Premium: $" + premium + " (Base: $" + testProduct.getBasePremium() + ")");
    }

    @Test
    void requiresUnderwriterReview_ShouldReturnTrue_ForHighRiskNiche() {
        UserPlatform cryptoProfile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.CRYPTO)
                .build();

        boolean requiresReview = riskPricingService.requiresUnderwriterReview(
                testProduct, cryptoProfile, testProduct.getCoverageAmount());

        assertThat(requiresReview).isTrue();
    }

    @Test
    void requiresUnderwriterReview_ShouldReturnTrue_ForHighCoverage() {
        UserPlatform profile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.LIFESTYLE)
                .build();

        boolean requiresReview = riskPricingService.requiresUnderwriterReview(
                testProduct, profile, BigDecimal.valueOf(600000));

        assertThat(requiresReview).isTrue();
    }

    @Test
    void requiresUnderwriterReview_ShouldReturnTrue_ForLowEngagement() {
        UserPlatform profile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(0.003)) // 0.3%
                .niche(Niche.LIFESTYLE)
                .build();

        boolean requiresReview = riskPricingService.requiresUnderwriterReview(
                testProduct, profile, testProduct.getCoverageAmount());

        assertThat(requiresReview).isTrue();
    }

    @Test
    void requiresUnderwriterReview_ShouldReturnFalse_ForNormalProfile() {
        UserPlatform profile = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(50000)
                .engagementRate(BigDecimal.valueOf(3.0))
                .niche(Niche.LIFESTYLE)
                .build();

        boolean requiresReview = riskPricingService.requiresUnderwriterReview(
                testProduct, profile, testProduct.getCoverageAmount());

        assertThat(requiresReview).isFalse();
    }

    @Test
    void calculateRiskScore_ShouldBeHigher_ForHighRiskProfile() {
        UserPlatform highRisk = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(500)
                .engagementRate(BigDecimal.valueOf(0.5))
                .niche(Niche.CRYPTO)
                .previousClaimsCount(2)
                .build();

        UserPlatform lowRisk = UserPlatform.builder()
                .platform(instagramPlatform)
                .followerCount(200000)
                .engagementRate(BigDecimal.valueOf(5.0))
                .niche(Niche.EDUCATION)
                .previousClaimsCount(0)
                .build();

        int highRiskScore = riskPricingService.calculateRiskScore(highRisk, testProduct);
        int lowRiskScore = riskPricingService.calculateRiskScore(lowRisk, testProduct);

        assertThat(highRiskScore).isGreaterThan(lowRiskScore);
        assertThat(highRiskScore).isBetween(0, 100);
        assertThat(lowRiskScore).isBetween(0, 100);
        
        System.out.println("High Risk Score: " + highRiskScore);
        System.out.println("Low Risk Score: " + lowRiskScore);
    }
    @Test
    void calculateBasePremium_ShouldThrowException_WhenProductIsNull() {
        assertThatThrownBy(() -> riskPricingService.calculateBasePremium(null, PlatformName.INSTAGRAM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product and its base premium must not be null");
    }

    @Test
    void calculatePersonalizedPremium_ShouldThrowException_WhenProfileIsNull() {
        assertThatThrownBy(() -> riskPricingService.calculatePersonalizedPremium(testProduct, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserProfile must not be null");
    }

    @Test
    void calculateRiskScore_ShouldThrowException_WhenProductIsNull() {
        UserPlatform profile = UserPlatform.builder().build();
        assertThatThrownBy(() -> riskPricingService.calculateRiskScore(profile, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Profile and Product must not be null");
    }
}
