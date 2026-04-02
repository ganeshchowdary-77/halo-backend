package com.thehalo.halobackend.mapper.policy;

import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.model.policy.Policy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface PolicyMapper {

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "profile.platform.name", target = "platformName")
    @Mapping(target = "insuredProfileId", source = "profile.id") // UserPlatform ID
    @Mapping(target = "insuredProfileHandle", source = "profile.handle")
    @Mapping(target = "renewalEligible", expression = "java(isRenewalEligible(policy))")
    PolicySummaryResponse toSummaryDto(Policy policy);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.tagline", target = "productTagline")
    @Mapping(source = "profile.id", target = "insuredProfileId") // UserPlatform ID
    @Mapping(source = "profile.handle", target = "insuredProfileHandle")
    @Mapping(source = "profile.platform.name", target = "insuredPlatform")
    @Mapping(source = "user.fullName", target = "holderName")
    @Mapping(source = "user.email", target = "holderEmail")
    @Mapping(source = "product.coveredLegal", target = "coverageLegal")
    @Mapping(source = "product.coverageLimitLegal", target = "limitLegal")
    @Mapping(source = "product.coveredReputation", target = "coverageReputation")
    @Mapping(source = "product.coverageLimitReputation", target = "limitReputation")
    @Mapping(source = "product.coveredCyber", target = "coverageCyber")
    @Mapping(source = "product.coverageLimitCyber", target = "limitCyber")
    @Mapping(source = "underwriter.fullName", target = "underwriterName")
    PolicyDetailResponse toDetailDto(Policy policy);

    default boolean isRenewalEligible(Policy policy) {
        LocalDate endDate = policy.getEndDate();
        return policy.getStatus() == com.thehalo.halobackend.enums.PolicyStatus.ACTIVE
                && endDate != null && endDate.isAfter(LocalDate.now().minusDays(30));
    }
}
