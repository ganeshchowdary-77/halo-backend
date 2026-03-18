package com.thehalo.halobackend.mapper.claim;

import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimDocumentResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.claim.ClaimDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ClaimMapper {

    @Mapping(source = "policy.policyNumber", target = "policyNumber")
    @Mapping(source = "profile.handle", target = "profileHandle")
    @Mapping(target = "filedAt", expression = "java(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)")
    ClaimSummaryResponse toSummaryDto(Claim c);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "claimNumber", target = "claimNumber")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "expenseType", target = "expenseType")
    @Mapping(source = "claimAmount", target = "claimAmount")
    @Mapping(source = "approvedAmount", target = "approvedAmount")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "incidentUrl", target = "incidentUrl")
    @Mapping(source = "incidentDate", target = "incidentDate")
    @Mapping(source = "createdAt", target = "filedAt")
    @Mapping(source = "reviewedAt", target = "reviewedAt")
    @Mapping(source = "officerComments", target = "officerComments")
    @Mapping(source = "policy.policyNumber", target = "policyNumber")
    @Mapping(source = "policy.product.name", target = "productName")
    @Mapping(source = "policy.totalCoverageLimit", target = "policyTotalCoverageLimit")
    @Mapping(source = "profile.handle", target = "profileHandle")
    @Mapping(target = "profilePlatform", expression = "java(getPlatformName(c.getProfile()))")
    @Mapping(target = "assignedOfficerName", expression = "java(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getFullName() : null)")
    @Mapping(target = "documents", ignore = true)
    ClaimDetailResponse toDetailDto(Claim c);

    // Used by ClaimDocumentService - maps a single document
    @Mapping(source = "id", target = "id")
    @Mapping(source = "fileName", target = "fileName")
    @Mapping(source = "filePath", target = "documentUrl", qualifiedByName = "filePathToUrl")
    @Mapping(source = "documentType", target = "documentType")
    @Mapping(source = "fileSizeBytes", target = "fileSizeBytes")
    @Mapping(source = "uploadedAt", target = "uploadedAt")
    ClaimDocumentResponse toDocumentDto(ClaimDocument d);

    default String getPlatformName(com.thehalo.halobackend.model.user.UserPlatform profile) {
        if (profile == null || profile.getPlatform() == null || profile.getPlatform().getName() == null) {
            return "Unknown";
        }
        return profile.getPlatform().getName().name();
    }

    @org.mapstruct.Named("filePathToUrl")
    default String filePathToUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) return null;
        // File paths are stored as "claims/5/uuid.pdf" or "uploads/claims/5/uuid.pdf"
        // Ensure we don't double-add "uploads/"
        if (filePath.startsWith("uploads/")) {
            return "http://localhost:8080/" + filePath;
        } else if (filePath.startsWith("claims/")) {
            return "http://localhost:8080/" + filePath;
        } else {
            return "http://localhost:8080/uploads/" + filePath;
        }
    }
}
