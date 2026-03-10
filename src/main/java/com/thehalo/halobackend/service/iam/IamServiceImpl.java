package com.thehalo.halobackend.service.iam;

import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.dto.iam.request.CreateStaffRequest;
import com.thehalo.halobackend.dto.iam.request.UpdateStaffRequest;
import com.thehalo.halobackend.dto.iam.response.StaffSummaryResponse;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.exception.business.DuplicateResourceException;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.exception.domain.auth.RoleNotFoundException;
import com.thehalo.halobackend.exception.validation.ValidationException;
import com.thehalo.halobackend.mapper.iam.IamMapper;
import com.thehalo.halobackend.model.profile.AppRole;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.repository.AppRoleRepository;
import com.thehalo.halobackend.repository.AppUserRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final IamMapper iamMapper;

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

    @Transactional(readOnly = true)
    public List<StaffSummaryResponse> getAllStaff() {
        List<AppUser> staffUsers = userRepository.findByRoleNameNot(RoleName.INFLUENCER);
        return iamMapper.toStaffSummaryList(staffUsers);
    }

    @Transactional(readOnly = true)
    public StaffSummaryResponse getStaffById(Long id) {
        AppUser user = userRepository.findByIdAndRoleNameNot(id, RoleName.INFLUENCER)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member with ID " + id));
        return iamMapper.toStaffSummary(user);
    }

    @Transactional
    public StaffSummaryResponse updateStaff(Long id, UpdateStaffRequest request) {
        AppUser user = userRepository.findByIdAndRoleNameNot(id, RoleName.INFLUENCER)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member with ID " + id));

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
                .orElseThrow(() -> new ResourceNotFoundException("Staff member with ID " + id));
        
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
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
}
