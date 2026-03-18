package com.thehalo.halobackend.mapper.platform;

import com.thehalo.halobackend.dto.platform.response.PlatformDetailResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;
import com.thehalo.halobackend.model.user.UserPlatform;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlatformMapper {

    @Mapping(source = "platform.name", target = "platformName")
    @Mapping(source = "user.fullName", target = "influencerName")
    @Mapping(source = "user.email", target = "influencerEmail")
    @Mapping(source = "handle", target = "handle")
    @Mapping(source = "platformUrl", target = "platformUrl")
    @Mapping(source = "followerCount", target = "followerCount")
    @Mapping(source = "niche", target = "niche")
    @Mapping(source = "verified", target = "verified")
    @Mapping(source = "nicheVerified", target = "nicheVerified")
    @Mapping(source = "addressVerified", target = "addressVerified")
    @Mapping(source = "incomeVerified", target = "incomeVerified")
    @Mapping(source = "engagementRate", target = "engagementRate")
    @Mapping(source = "verificationStatus", target = "verificationStatus")
    @Mapping(source = "riskLevel", target = "riskLevel")
    @Mapping(source = "nicheRejectionReason", target = "nicheRejectionReason")
    @Mapping(source = "addressRejectionReason", target = "addressRejectionReason")
    @Mapping(source = "incomeRejectionReason", target = "incomeRejectionReason")
    @Mapping(source = "addressProofPath", target = "addressProofPath")
    @Mapping(source = "incomeProofPath", target = "incomeProofPath")
    @Mapping(target = "addressProofUrl", ignore = true)
    @Mapping(target = "incomeProofUrl", ignore = true)
    @Mapping(target = "hasActivePolicy", ignore = true)
    PlatformSummaryResponse toSummaryDto(UserPlatform userPlatform);

    @Mapping(source = "platform.name", target = "platformName")
    @Mapping(source = "platform.description", target = "platformDescription")
    @Mapping(source = "addressProofPath", target = "addressProofPath")
    @Mapping(source = "incomeProofPath", target = "incomeProofPath")
    @Mapping(target = "hasActivePolicy", ignore = true)
    PlatformDetailResponse toDetailDto(UserPlatform userPlatform);
}
