package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.claim.response.ClaimDocumentResponse;
import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.service.claim.ClaimDocumentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimDocumentController {

    private final ClaimDocumentService documentService;

    @PostMapping(value = "/{claimId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<ClaimDocumentResponse>> uploadDocument(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        
        ClaimDocumentResponse response = documentService.uploadDocument(claimId, file, documentType);
        return ResponseFactory.success(response, "Document uploaded successfully", HttpStatus.CREATED);
    }

    @GetMapping("/{claimId}/documents")
    @PreAuthorize("hasAnyRole('INFLUENCER', 'CLAIMS_OFFICER')")
    public ResponseEntity<HaloApiResponse<List<ClaimDocumentResponse>>> getClaimDocuments(
            @PathVariable Long claimId) {
        
        List<ClaimDocumentResponse> documents = documentService.getClaimDocuments(claimId);
        return ResponseFactory.success(documents, "Documents retrieved successfully");
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<HaloApiResponse<Void>> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return ResponseFactory.success(null, "Document deleted successfully");
    }

    @PostMapping("/{claimId}/request-documents")
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    public ResponseEntity<HaloApiResponse<Void>> requestAdditionalDocuments(
            @PathVariable Long claimId,
            @RequestBody RequestDocumentsRequest request) {
        
        documentService.requestAdditionalDocuments(claimId, request.getMessage());
        return ResponseFactory.success(null, "Document request sent to claimant");
    }

    @Getter
    @Setter
    public static class RequestDocumentsRequest {
        private String message;
    }
}
