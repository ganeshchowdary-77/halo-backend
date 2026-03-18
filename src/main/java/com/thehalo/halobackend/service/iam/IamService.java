package com.thehalo.halobackend.service.iam;

import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.dto.iam.request.CreateStaffRequest;
import com.thehalo.halobackend.dto.iam.request.UpdateStaffRequest;
import com.thehalo.halobackend.dto.iam.response.StaffSummaryResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;

import java.util.List;

public interface IamService {
    AuthResponse createStaff(CreateStaffRequest request);
    List<StaffSummaryResponse> getAllStaff();
    StaffSummaryResponse getStaffById(Long id);
    StaffSummaryResponse updateStaff(Long id, UpdateStaffRequest request);
    void deactivateStaff(Long id);
    List<StaffSummaryResponse> getStaffByRole(String role);
    
    // Influencer verification
    List<PlatformSummaryResponse> getUnverifiedProfiles();
    PlatformSummaryResponse verifyProfile(Long profileId);
    com.thehalo.halobackend.dto.platform.response.PlatformVerificationResponse verifyAspect(Long profileId, String verificationType, Boolean approved, String rejectionReason);
}
