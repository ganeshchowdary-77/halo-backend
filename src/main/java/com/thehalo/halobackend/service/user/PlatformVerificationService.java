package com.thehalo.halobackend.service.user;

import com.thehalo.halobackend.dto.platform.request.VerifyPlatformRequest;
import com.thehalo.halobackend.dto.platform.response.PlatformVerificationResponse;
import com.thehalo.halobackend.enums.PlatformVerificationStatus;
import com.thehalo.halobackend.enums.RiskLevel;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Enhanced Platform Verification Service
 * Handles verification with address proof, ID verification, and previous cases tracking
 */
@Service
@RequiredArgsConstructor
public class PlatformVerificationService {

    private final UserPlatformRepository platformRepository;

    /**
     * Get all platforms pending verification (for IAM Admin)
     */
    public List<PlatformVerificationResponse> getPendingVerifications() {
        return platformRepository.findByVerificationStatus(PlatformVerificationStatus.PENDING)
                .stream()
                .map(this::toVerificationResponse)
                .toList();
    }

    /**
     * Verify a platform with enhanced verification data
     */
    @Transactional
    public PlatformVerificationResponse verifyPlatform(Long platformId, VerifyPlatformRequest request) {
        UserPlatform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> new ProfileNotFoundException(platformId));

        Long currentUserId = getCurrentUserId();
        AppUser verifier = new AppUser();
        verifier.setId(currentUserId);

        // Update verification fields
        platform.setVerified(true);
        platform.setVerificationStatus(PlatformVerificationStatus.APPROVED);
        platform.setVerifiedAt(LocalDateTime.now());
        platform.setVerifiedBy(verifier);
        platform.setVerificationNotes(request.getVerificationNotes());
        platform.setAddressProofPath(request.getAddressProofPath());
        platform.setIdVerificationPath(request.getIdVerificationPath());
        
        // Calculate and set risk level
        platform.setRiskLevel(calculateRiskLevel(platform));
        
        // Update previous cases if provided
        if (request.getPreviousClaimsCount() != null) {
            platform.setPreviousClaimsCount(request.getPreviousClaimsCount());
        }
        if (request.getPreviousClaimsAmount() != null) {
            platform.setPreviousClaimsAmount(request.getPreviousClaimsAmount());
        }

        UserPlatform saved = platformRepository.save(platform);
        return toVerificationResponse(saved);
    }

    /**
     * Reject a platform verification
     */
    @Transactional
    public PlatformVerificationResponse rejectPlatform(Long platformId, String reason) {
        UserPlatform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> new ProfileNotFoundException(platformId));

        platform.setVerified(false);
        platform.setVerificationStatus(PlatformVerificationStatus.REJECTED);
        platform.setRejectionReason(reason);
        platform.setVerificationNotes(reason);
        
        UserPlatform saved = platformRepository.save(platform);
        return toVerificationResponse(saved);
    }

    /**
     * Granular verification for specific aspects (niche, address, income)
     */
    @Transactional
    public PlatformVerificationResponse verifyAspect(Long platformId, String verificationType, Boolean approved, String rejectionReason) {
        UserPlatform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> new ProfileNotFoundException(platformId));

        switch (verificationType.toUpperCase()) {
            case "NICHE":
                platform.setNicheVerified(approved);
                if (!approved) {
                    platform.setNicheRejectionReason(rejectionReason);
                } else {
                    platform.setNicheRejectionReason(null);
                }
                break;
            case "ADDRESS":
                platform.setAddressVerified(approved);
                if (!approved) {
                    platform.setAddressRejectionReason(rejectionReason);
                } else {
                    platform.setAddressRejectionReason(null);
                }
                break;
            case "INCOME":
                platform.setIncomeVerified(approved);
                if (!approved) {
                    platform.setIncomeRejectionReason(rejectionReason);
                } else {
                    platform.setIncomeRejectionReason(null);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid verification type: " + verificationType);
        }

        // Check if all aspects are verified (all must be explicitly TRUE)
        if (Boolean.TRUE.equals(platform.getNicheVerified()) && 
            Boolean.TRUE.equals(platform.getAddressVerified()) && 
            Boolean.TRUE.equals(platform.getIncomeVerified())) {
            
            platform.setVerified(true);
            platform.setVerificationStatus(PlatformVerificationStatus.APPROVED);
            platform.setVerifiedAt(LocalDateTime.now());
            
            Long currentUserId = getCurrentUserId();
            AppUser verifier = new AppUser();
            verifier.setId(currentUserId);
            platform.setVerifiedBy(verifier);
            
            // Calculate and set risk level
            platform.setRiskLevel(calculateRiskLevel(platform));
        } 
        // Only mark as rejected if at least one aspect was explicitly rejected (FALSE, not null)
        else if (Boolean.FALSE.equals(platform.getNicheVerified()) || 
                   Boolean.FALSE.equals(platform.getAddressVerified()) || 
                   Boolean.FALSE.equals(platform.getIncomeVerified())) {
            
            // If any aspect is rejected, mark as rejected
            platform.setVerified(false);
            platform.setVerificationStatus(PlatformVerificationStatus.REJECTED);
            
            // Combine rejection reasons
            StringBuilder combinedReason = new StringBuilder();
            if (Boolean.FALSE.equals(platform.getNicheVerified()) && platform.getNicheRejectionReason() != null) {
                combinedReason.append("Niche: ").append(platform.getNicheRejectionReason()).append("; ");
            }
            if (Boolean.FALSE.equals(platform.getAddressVerified()) && platform.getAddressRejectionReason() != null) {
                combinedReason.append("Address: ").append(platform.getAddressRejectionReason()).append("; ");
            }
            if (Boolean.FALSE.equals(platform.getIncomeVerified()) && platform.getIncomeRejectionReason() != null) {
                combinedReason.append("Income: ").append(platform.getIncomeRejectionReason()).append("; ");
            }
            platform.setRejectionReason(combinedReason.toString());
        }
        // Otherwise, keep status as PENDING (some aspects verified, some still pending)

        UserPlatform saved = platformRepository.save(platform);
        return toVerificationResponse(saved);
    }

    /**
     * Calculate risk level based on platform characteristics
     */
    private RiskLevel calculateRiskLevel(UserPlatform platform) {
        int riskScore = 0;
        
        // Platform type risk
        switch (platform.getPlatform().getName()) {
            case X -> riskScore += 30;
            case INSTAGRAM -> riskScore += 10;
            case LINKEDIN -> riskScore += 5;
            case TIKTOK -> riskScore += 25;
            case YOUTUBE -> riskScore += 15;
            case FACEBOOK -> riskScore += 12;
            case SNAPCHAT -> riskScore += 20;
        }
        
        // Follower count risk (very high = higher risk due to visibility)
        if (platform.getFollowerCount() > 5_000_000) riskScore += 25;
        else if (platform.getFollowerCount() > 1_000_000) riskScore += 15;
        else if (platform.getFollowerCount() > 100_000) riskScore += 10;
        
        // Engagement rate risk (very low = bot followers, very high = controversial)
        if (platform.getEngagementRate().compareTo(BigDecimal.valueOf(0.5)) < 0) riskScore += 20; // Too low
        if (platform.getEngagementRate().compareTo(BigDecimal.valueOf(15.0)) > 0) riskScore += 15; // Too high
        
        // Niche risk
        switch (platform.getNiche()) {
            case CRYPTO, FINANCE -> riskScore += 30;
            case POLITICS -> riskScore += 25;
            case GAMING -> riskScore += 15;
            case LIFESTYLE, BEAUTY -> riskScore += 5;
            case ENTERTAINMENT, COMEDY, FASHION, MUSIC, TRAVEL, FOOD, SPORTS, FITNESS -> riskScore += 10;
            case TECHNOLOGY, BUSINESS, EDUCATION -> riskScore += 8;
            case OTHER -> riskScore += 20; // Custom niches need review
        }
        
        // Previous claims history
        if (platform.getPreviousClaimsCount() != null && platform.getPreviousClaimsCount() > 0) {
            riskScore += platform.getPreviousClaimsCount() * 10;
        }
        
        // Convert score to risk level
        if (riskScore >= 70) return RiskLevel.VERY_HIGH;
        if (riskScore >= 50) return RiskLevel.HIGH;
        if (riskScore >= 30) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    /**
     * Update previous cases for a platform (when claims are processed)
     */
    @Transactional
    public void updatePreviousCases(Long platformId, int additionalClaims, BigDecimal additionalAmount) {
        UserPlatform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> new ProfileNotFoundException(platformId));

        int currentCount = platform.getPreviousClaimsCount() != null ? platform.getPreviousClaimsCount() : 0;
        BigDecimal currentAmount = platform.getPreviousClaimsAmount() != null ? 
            platform.getPreviousClaimsAmount() : BigDecimal.ZERO;

        platform.setPreviousClaimsCount(currentCount + additionalClaims);
        platform.setPreviousClaimsAmount(currentAmount.add(additionalAmount));

        platformRepository.save(platform);
    }

    private PlatformVerificationResponse toVerificationResponse(UserPlatform platform) {
        // Convert file paths to full URLs using the correct endpoint
        String addressProofUrl = platform.getAddressProofPath() != null ? 
            "http://localhost:8080/uploads/" + platform.getAddressProofPath() : null;
        String incomeProofUrl = platform.getIncomeProofPath() != null ? 
            "http://localhost:8080/uploads/" + platform.getIncomeProofPath() : null;
            
        return PlatformVerificationResponse.builder()
                .id(platform.getId())
                .userId(platform.getUser().getId())
                .userEmail(platform.getUser().getEmail())
                .influencerName(platform.getUser().getFullName())
                .influencerEmail(platform.getUser().getEmail())
                .platformName(platform.getPlatform().getName().name())
                .handle(platform.getHandle())
                .platformUrl(platform.getPlatformUrl())
                .followerCount(platform.getFollowerCount())
                .engagementRate(platform.getEngagementRate())
                .niche(platform.getNiche().name())
                .verified(platform.getVerified())
                .verificationStatus(platform.getVerificationStatus())
                .riskLevel(platform.getRiskLevel())
                .verificationNotes(platform.getVerificationNotes())
                .rejectionReason(platform.getRejectionReason())
                .addressProofPath(platform.getAddressProofPath())
                .addressProofUrl(addressProofUrl)
                .idVerificationPath(platform.getIdVerificationPath())
                .incomeProofPath(platform.getIncomeProofPath())
                .incomeProofUrl(incomeProofUrl)
                .previousClaimsCount(platform.getPreviousClaimsCount())
                .previousClaimsAmount(platform.getPreviousClaimsAmount())
                .verifiedAt(platform.getVerifiedAt())
                .verifiedByEmail(platform.getVerifiedBy() != null ? platform.getVerifiedBy().getEmail() : null)
                .createdAt(platform.getCreatedAt())
                // Granular verification fields
                .nicheVerified(platform.getNicheVerified())
                .nicheRejectionReason(platform.getNicheRejectionReason())
                .addressVerified(platform.getAddressVerified())
                .addressRejectionReason(platform.getAddressRejectionReason())
                .incomeVerified(platform.getIncomeVerified())
                .incomeRejectionReason(platform.getIncomeRejectionReason())
                .build();
    }

    private Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }
}