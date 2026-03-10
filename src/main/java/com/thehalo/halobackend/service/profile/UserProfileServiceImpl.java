package com.thehalo.halobackend.service.profile;

import com.thehalo.halobackend.dto.profile.request.AddProfileRequest;
import com.thehalo.halobackend.dto.profile.response.ProfileDetailResponse;
import com.thehalo.halobackend.dto.profile.response.ProfileSummaryResponse;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.PlatformHandleAlreadyLinkedException;
import com.thehalo.halobackend.mapper.profile.ProfileMapper;
import com.thehalo.halobackend.model.profile.Platform;
import com.thehalo.halobackend.model.profile.UserProfile;
import com.thehalo.halobackend.repository.PlatformRepository;
import com.thehalo.halobackend.repository.UserProfileRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Manages social media profiles linked to an influencer's account
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final PlatformRepository platformRepository;
    private final ProfileMapper profileMapper;

    // Returns all profiles linked to the currently authenticated influencer
    @Transactional(readOnly = true)
    public List<ProfileSummaryResponse> getMyProfiles() {
        Long userId = currentUserId();
        return profileRepository.findByUserId(userId)
                .stream().map(profileMapper::toSummaryDto).toList();
    }

    // Returns full detail of a single profile — validates ownership
    @Transactional(readOnly = true)
    public ProfileDetailResponse getProfile(Long profileId) {
        Long userId = currentUserId();
        UserProfile p = profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return profileMapper.toDetailDto(p);
    }

    // Links a new social media channel to the current user's account
    @Transactional
    public ProfileSummaryResponse addProfile(AddProfileRequest request) {
        Platform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new ResourceNotFoundException("Platform not found: " + request.getPlatformId()));

        // Prevent duplicate handles on same platform across all users
        profileRepository.findByPlatformIdAndHandle(request.getPlatformId(), request.getHandle())
                .ifPresent(p -> {
                    throw new PlatformHandleAlreadyLinkedException(request.getHandle());
                });

        UserProfile profile = UserProfile.builder()
                .user(currentUser())
                .platform(platform)
                .handle(request.getHandle())
                .profileUrl(request.getProfileUrl())
                .followerCount(request.getFollowerCount().intValue())
                .engagementRate(BigDecimal.valueOf(request.getEngagementRate()))
                .niche(request.getNiche())
                .verified(false)
                .build();

        return profileMapper.toSummaryDto(profileRepository.save(profile));
    }

    // Removes a linked profile — only the owner can delete their own profile
    @Transactional
    public void deleteProfile(Long profileId) {
        Long userId = currentUserId();
        UserProfile profile = profileRepository.findByIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        profileRepository.delete(profile);
    }

    // Resolves the current user's ID from the security context
    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }

    // Resolves the full AppUser from security context via a lazy proxy
    private com.thehalo.halobackend.model.profile.AppUser currentUser() {
        var ref = new com.thehalo.halobackend.model.profile.AppUser();
        ref.setId(currentUserId());
        return ref;
    }
}
