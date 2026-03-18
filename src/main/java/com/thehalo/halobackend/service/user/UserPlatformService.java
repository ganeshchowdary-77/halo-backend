package com.thehalo.halobackend.service.user;

import com.thehalo.halobackend.dto.platform.request.AddPlatformRequest;
import com.thehalo.halobackend.dto.platform.request.UpdatePlatformRequest;
import com.thehalo.halobackend.dto.platform.response.PlatformDetailResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;

import java.util.List;

public interface UserPlatformService {
    List<PlatformSummaryResponse> getMyPlatforms();

    PlatformDetailResponse getPlatform(Long platformId);

    PlatformSummaryResponse addPlatform(AddPlatformRequest request, org.springframework.web.multipart.MultipartFile addressProof, org.springframework.web.multipart.MultipartFile incomeProof);

    PlatformDetailResponse updatePlatform(Long platformId, UpdatePlatformRequest request);

    void deletePlatform(Long platformId);
    
    boolean hasVerifiedPlatforms(Long userId);
    
    boolean hasAnyPlatforms(Long userId);
}
