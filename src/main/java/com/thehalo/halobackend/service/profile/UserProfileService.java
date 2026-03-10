package com.thehalo.halobackend.service.profile;

import com.thehalo.halobackend.dto.profile.request.AddProfileRequest;
import com.thehalo.halobackend.dto.profile.response.ProfileDetailResponse;
import com.thehalo.halobackend.dto.profile.response.ProfileSummaryResponse;

import java.util.List;

public interface UserProfileService {
    List<ProfileSummaryResponse> getMyProfiles();

    ProfileDetailResponse getProfile(Long profileId);

    ProfileSummaryResponse addProfile(AddProfileRequest request);

    void deleteProfile(Long profileId);
}
