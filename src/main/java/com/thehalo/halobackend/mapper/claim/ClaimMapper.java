package com.thehalo.halobackend.mapper.claim;

import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimDocumentResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimTimelineResponse;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.claim.ClaimDocument;
import com.thehalo.halobackend.model.claim.ClaimTimeline;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    @Mapping(source = "policy.policyNumber", target = "policyNumber")
    @Mapping(source = "profile.handle", target = "profileHandle")
    @Mapping(target = "filedAt", expression = "java(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)")
    ClaimSummaryResponse toSummaryDto(Claim c);

    @Mapping(source = "policy.policyNumber", target = "policyNumber")
    @Mapping(source = "policy.product.name", target = "productName")
    @Mapping(source = "policy.totalCoverageLimit", target = "policyTotalCoverageLimit")
    @Mapping(source = "profile.handle", target = "profileHandle")
    @Mapping(source = "profile.platform.name", target = "profilePlatform")
    @Mapping(target = "assignedOfficerName", expression = "java(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getFullName() : null)")
    @Mapping(target = "filedAt", source = "createdAt")
    ClaimDetailResponse toDetailDto(Claim c);

    ClaimDocumentResponse toDocumentDto(ClaimDocument d);

    @Mapping(target = "changedBy", expression = "java(t.getChangedBy() != null ? t.getChangedBy().getEmail() : \"system\")")
    ClaimTimelineResponse toTimelineDto(ClaimTimeline t);
}
