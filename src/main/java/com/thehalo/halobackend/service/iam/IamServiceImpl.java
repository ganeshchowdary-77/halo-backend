package com.thehalo.halobackend.service.iam;

import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.dto.iam.request.CreateStaffRequest;
import com.thehalo.halobackend.dto.iam.request.UpdateStaffRequest;
import com.thehalo.halobackend.dto.iam.response.StaffSummaryResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.exception.business.DuplicateResourceException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.exception.domain.iam.StaffNotFoundException;
import com.thehalo.halobackend.exception.domain.auth.RoleNotFoundException;
import com.thehalo.halobackend.exception.validation.ValidationException;
import com.thehalo.halobackend.mapper.iam.IamMapper;
import com.thehalo.halobackend.mapper.platform.PlatformMapper;
import com.thehalo.halobackend.model.user.AppRole;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.AppRoleRepository;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IamServiceImpl implements IamService {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final UserPlatformRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final IamMapper iamMapper;
    private final PlatformMapper platformMapper;

    @Transactional
    public AuthResponse createStaff(CreateStaffRequest request) {

        if (request.getRole() == RoleName.INFLUENCER) {
            throw new ValidationException("Cannot create INFLUENCER accounts via IAM portal. Use /api/v1/auth/register instead.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "'");
        }

        AppRole role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RoleNotFoundException(request.getRole().name()));

        AppUser user = new AppUser();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        AppUser savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().getName().name())
                .build();
    }

    public List<StaffSummaryResponse> getAllStaff() {
        List<AppUser> staffUsers = userRepository.findByRoleNameNot(RoleName.INFLUENCER);
        return iamMapper.toStaffSummaryList(staffUsers);
    }

    public StaffSummaryResponse getStaffById(Long id) {
        AppUser user = userRepository.findByIdAndRoleNameNot(id, RoleName.INFLUENCER)
                .orElseThrow(() -> new StaffNotFoundException(id));
        return iamMapper.toStaffSummary(user);
    }

    @Transactional
    public StaffSummaryResponse updateStaff(Long id, UpdateStaffRequest request) {
        AppUser user = userRepository.findByIdAndRoleNameNot(id, RoleName.INFLUENCER)
                .orElseThrow(() -> new StaffNotFoundException(id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email '" + request.getEmail() + "'");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        AppUser savedUser = userRepository.save(user);
        return iamMapper.toStaffSummary(savedUser);
    }

    @Transactional
    public void deactivateStaff(Long id) {
        AppUser user = userRepository.findByIdAndRoleNameNot(id, RoleName.INFLUENCER)
                .orElseThrow(() -> new StaffNotFoundException(id));
        
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<StaffSummaryResponse> getStaffByRole(String roleName) {
        try {
            RoleName role = RoleName.valueOf(roleName.toUpperCase());
            if (role == RoleName.INFLUENCER) {
                throw new ValidationException("Cannot retrieve INFLUENCER accounts via IAM endpoints");
            }
            List<AppUser> staffUsers = userRepository.findByRoleName(role);
            return iamMapper.toStaffSummaryList(staffUsers);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid role name: " + roleName);
        }
    }

    public List<PlatformSummaryResponse> getUnverifiedProfiles() {
        List<UserPlatform> unverifiedProfiles = profileRepository.findByVerifiedFalse();
        return unverifiedProfiles.stream()
                .map(platform -> {
                    PlatformSummaryResponse dto = platformMapper.toSummaryDto(platform);
                    // Manually generate document URLs only if paths don't already contain URLs
                    dto.setAddressProofUrl(ensureFullUrl(platform.getAddressProofPath()));
                    dto.setIncomeProofUrl(ensureFullUrl(platform.getIncomeProofPath()));
                    return dto;
                })
                .toList();
    }
    
    private String ensureFullUrl(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) {
            return null;
        }
        // If it's already a full URL, return as-is
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl;
        }
        // Otherwise, convert path to URL
        if (pathOrUrl.startsWith("uploads/")) {
            return "http://localhost:8080/" + pathOrUrl;
        } else if (pathOrUrl.startsWith("platforms/") || pathOrUrl.startsWith("claims/")) {
            return "http://localhost:8080/" + pathOrUrl;
        } else {
            return "http://localhost:8080/uploads/" + pathOrUrl;
        }
    }

    @Transactional
    public PlatformSummaryResponse verifyProfile(Long profileId) {
        UserPlatform profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException(profileId));
        
        profile.setVerified(true);
        profile.setVerifiedAt(LocalDateTime.now());
        profile.setVerifiedBy(currentUser());
        
        UserPlatform savedProfile = profileRepository.save(profile);
        return platformMapper.toSummaryDto(savedProfile);
    }

    @Transactional
    public com.thehalo.halobackend.dto.platform.response.PlatformVerificationResponse verifyAspect(Long profileId, String verificationType, Boolean approved, String rejectionReason) {
        // Delegate to PlatformVerificationService
        com.thehalo.halobackend.service.user.PlatformVerificationService verificationService = 
            new com.thehalo.halobackend.service.user.PlatformVerificationService(profileRepository);
        return verificationService.verifyAspect(profileId, verificationType, approved, rejectionReason);
    }

    private Long currentUserId() {
        return ((com.thehalo.halobackend.security.service.CustomUserDetails) org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }

    private AppUser currentUser() {
        AppUser user = new AppUser();
        user.setId(currentUserId());
        return user;
    }
}
