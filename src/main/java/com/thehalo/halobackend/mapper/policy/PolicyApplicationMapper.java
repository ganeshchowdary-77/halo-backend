package com.thehalo.halobackend.mapper.policy;

import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationResponse;
import com.thehalo.halobackend.model.policy.PolicyApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PolicyApplicationMapper {

    @Mapping(source = "applicationNumber", target = "applicationNumber")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.tagline", target = "productTagline")
    @Mapping(source = "product.basePremium", target = "basePremium")
    @Mapping(source = "profile.id", target = "profileId")
    @Mapping(source = "profile.handle", target = "profileHandle")
    @Mapping(source = "profile.platform.name", target = "platformName")
    @Mapping(source = "profile.followerCount", target = "followerCount")
    @Mapping(source = "profile.engagementRate", target = "engagementRate")
    @Mapping(target = "niche", expression = "java(app.getProfile().getNiche() != null ? app.getProfile().getNiche().name() : null)")
    @Mapping(target = "influencerName", expression = "java(app.getUser() != null ? app.getUser().getFullName() : null)")
    @Mapping(target = "riskLevel", expression = "java(getRiskLevel(app.getRiskScore()))")
    @Mapping(target = "assignedUnderwriterName", expression = "java(app.getAssignedUnderwriter() != null ? app.getAssignedUnderwriter().getFullName() : null)")
    PolicyApplicationDetailResponse toDetailDto(PolicyApplication app);

    @Mapping(source = "applicationNumber", target = "applicationNumber")
    @Mapping(source = "user.fullName", target = "insurerName")
    @Mapping(source = "profile.platform.name", target = "platform")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "createdAt", target = "requestedAt")
    PolicyApplicationResponse toSummaryDto(PolicyApplication app);

    default String getRiskLevel(Integer riskScore) {
        if (riskScore == null) return "UNKNOWN";
        if (riskScore > 70) return "HIGH";
        if (riskScore < 40) return "LOW";
        return "MEDIUM";
    }
}
