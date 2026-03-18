package com.thehalo.halobackend.service.user;

import com.thehalo.halobackend.dto.platform.request.AddPlatformRequest;
import com.thehalo.halobackend.dto.platform.request.UpdatePlatformRequest;
import com.thehalo.halobackend.dto.platform.response.PlatformDetailResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;
import com.thehalo.halobackend.enums.PlatformVerificationStatus;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.PlatformNotFoundException;
import com.thehalo.halobackend.mapper.platform.PlatformMapper;
import com.thehalo.halobackend.model.platform.Platform;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.PlatformRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Manages social media platforms linked to an influencer's account
@Service
@RequiredArgsConstructor
public class UserPlatformServiceImpl implements UserPlatformService {

    private final UserPlatformRepository platformRepository;
    private final PlatformRepository socialPlatformRepository;
    private final PolicyRepository policyRepository;
    private final PlatformMapper platformMapper;

    // Returns all platforms linked to the currently authenticated influencer
    @Override
    public List<PlatformSummaryResponse> getMyPlatforms() {
        Long userId = currentUserId();
        return platformRepository.findByUserId(userId)
                .stream()
                .map(platform -> {
                    PlatformSummaryResponse dto = platformMapper.toSummaryDto(platform);
                    // Check if platform has an active policy
                    boolean hasActivePolicy = policyRepository.findByUserIdAndStatus(userId, PolicyStatus.ACTIVE)
                        .stream()
                        .anyMatch(policy -> policy.getProfile() != null && policy.getProfile().getId().equals(platform.getId()));
                    dto.setHasActivePolicy(hasActivePolicy);
                    return dto;
                })
                .toList();
    }

    // Returns full detail of a single platform — validates ownership
    @Override
    public PlatformDetailResponse getPlatform(Long platformId) {
        Long userId = currentUserId();
        UserPlatform p = platformRepository.findByIdAndUserId(platformId, userId)
                .orElseThrow(() -> new ProfileNotFoundException(platformId));
        PlatformDetailResponse dto = platformMapper.toDetailDto(p);
        // Check if platform has an active policy
        boolean hasActivePolicy = policyRepository.findByUserIdAndStatus(userId, PolicyStatus.ACTIVE)
            .stream()
            .anyMatch(policy -> policy.getProfile() != null && policy.getProfile().getId().equals(platformId));
        dto.setHasActivePolicy(hasActivePolicy);
        return dto;
    }

    private final com.thehalo.halobackend.service.common.FileStorageService fileStorageService;

    // Links a new social media channel to the current user's account
    @Transactional
    @Override
    public PlatformSummaryResponse addPlatform(AddPlatformRequest request, 
                                        org.springframework.web.multipart.MultipartFile addressProof, 
                                        org.springframework.web.multipart.MultipartFile incomeProof) {
        
        Platform platform = socialPlatformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new PlatformNotFoundException(request.getPlatformId()));

        Long userId = currentUserId();

        // Prevent duplicate handles on same platform across all users
        platformRepository.findByPlatformIdAndHandle(request.getPlatformId(), request.getHandle())
                .ifPresent(p -> {
                    throw new com.thehalo.halobackend.exception.domain.profile.PlatformHandleAlreadyLinkedException(request.getHandle());
                });

        // Store proofs
        String addressPath = fileStorageService.storeFile(addressProof, "platforms", userId);
        String incomePath = fileStorageService.storeFile(incomeProof, "platforms", userId);

        UserPlatform userPlatform = UserPlatform.builder()
                .user(currentUser())
                .platform(platform)
                .handle(request.getHandle())
                .mockAccountHandle(request.getMockAccountHandle())
                .followerCount(request.getFollowerCount().intValue())
                .engagementRate(BigDecimal.valueOf(request.getEngagementRate()))
                .niche(request.getNiche())
                .customNiche(request.getCustomNiche())
                .addressProofPath(addressPath)
                .incomeProofPath(incomePath)
                .verified(false)
                .verificationStatus(PlatformVerificationStatus.PENDING)
                .build();

        // If niche is OTHER, set verified to false and require underwriter approval
        if (request.getNiche().name().equals("OTHER")) {
            userPlatform.setVerified(false);
        }

        return platformMapper.toSummaryDto(platformRepository.save(userPlatform));
    }

    // Updates an existing social media channel
    @Transactional
    @Override
    public PlatformDetailResponse updatePlatform(Long platformId, UpdatePlatformRequest request) {
        Long userId = currentUserId();
        UserPlatform platform = platformRepository.findByIdAndUserId(platformId, userId)
                .orElseThrow(() -> new ProfileNotFoundException(platformId));

        platform.setPlatformUrl(request.getPlatformUrl());
        platform.setFollowerCount(request.getFollowerCount().intValue());
        platform.setEngagementRate(BigDecimal.valueOf(request.getEngagementRate()));
        platform.setNiche(request.getNiche());
        
        // If platform was rejected, reset verification status for re-review
        if (platform.getVerificationStatus() == PlatformVerificationStatus.REJECTED) {
            platform.setVerificationStatus(PlatformVerificationStatus.PENDING);
            platform.setVerified(false);
            platform.setRejectionReason(null);
            
            // Clear granular rejection reasons
            platform.setNicheRejectionReason(null);
            platform.setAddressRejectionReason(null);
            platform.setIncomeRejectionReason(null);
            
            // Reset granular verification flags to null (pending review)
            platform.setNicheVerified(null);
            platform.setAddressVerified(null);
            platform.setIncomeVerified(null);
            
            // Clear verification metadata
            platform.setVerifiedAt(null);
            platform.setVerifiedBy(null);
            platform.setVerificationNotes(null);
        }

        return platformMapper.toDetailDto(platformRepository.save(platform));
    }

    // Removes a linked platform — only the owner can delete their own platform
    @Transactional
    @Override
    public void deletePlatform(Long platformId) {
        Long userId = currentUserId();
        UserPlatform platform = platformRepository.findByIdAndUserId(platformId, userId)
                .orElseThrow(() -> new ProfileNotFoundException(platformId));
        platformRepository.delete(platform);
    }

    // Resolves the current user's ID from the security context
    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }

    // Resolves the full AppUser from security context via a lazy proxy
    private AppUser currentUser() {
        var ref = new AppUser();
        ref.setId(currentUserId());
        return ref;
    }

    @Override
    public boolean hasVerifiedPlatforms(Long userId) {
        return platformRepository.existsByUserIdAndVerifiedTrue(userId);
    }

    @Override
    public boolean hasAnyPlatforms(Long userId) {
        return platformRepository.existsByUserId(userId);
    }
}
