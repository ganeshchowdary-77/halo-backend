package com.thehalo.halobackend.service.claim;

import com.thehalo.halobackend.dto.claim.response.ClaimDocumentResponse;
import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.exception.domain.claim.ClaimNotFoundException;
import com.thehalo.halobackend.exception.domain.claim.ClaimNotModifiableException;
import com.thehalo.halobackend.exception.domain.claim.DocumentNotFoundException;
import com.thehalo.halobackend.service.common.FileStorageService;
import com.thehalo.halobackend.mapper.claim.ClaimMapper;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.claim.ClaimDocument;
import com.thehalo.halobackend.repository.ClaimRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimDocumentService {

    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public ClaimDocumentResponse uploadDocument(Long claimId, MultipartFile file, String documentType) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        // Verify ownership
        Long currentUserId = getCurrentUserId();
        if (!claim.getPolicy().getUser().getId().equals(currentUserId)) {
            throw new ClaimNotModifiableException(claimId, "You can only upload documents to your own claims");
        }

        // Only allow uploads for SUBMITTED, UNDER_REVIEW, or PENDING_INFORMATION claims
        if (claim.getStatus() != ClaimStatus.SUBMITTED && claim.getStatus() != ClaimStatus.UNDER_REVIEW
                && claim.getStatus() != ClaimStatus.PENDING_INFORMATION) {
            throw new ClaimNotModifiableException(claimId, "Cannot upload documents to a " + claim.getStatus() + " claim");
        }

        // Use unified file storage service
        String uniqueFilenamePath = fileStorageService.storeFile(file, "claims", currentUserId);
        String originalFilename = file.getOriginalFilename();

            ClaimDocument document = ClaimDocument.builder()
                    .claim(claim)
                    .fileName(originalFilename)
                    .filePath(uniqueFilenamePath)
                    .documentType(documentType)
                    .fileSizeBytes(file.getSize())
                    .build(); // uploadedAt is set by @CreationTimestamp

            // Add to claim's documents collection
            if (claim.getDocuments() == null) {
                claim.setDocuments(new java.util.ArrayList<>());
            }
            claim.getDocuments().add(document);
            
            // Save the claim (cascade will save the document)
            Claim savedClaim = claimRepository.save(claim);

            // Find the saved document from the claim
            ClaimDocument savedDocument = savedClaim.getDocuments().stream()
                    .filter(d -> d.getFileName().equals(originalFilename))
                    .reduce((first, second) -> second) // Get the last one
                    .orElse(document);

            return claimMapper.toDocumentDto(savedDocument);
    }

    @Transactional(readOnly = true)
    public List<ClaimDocumentResponse> getClaimDocuments(Long claimId) {
        Claim claim = claimRepository.findByIdWithDocuments(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        return claim.getDocuments().stream()
                .map(claimMapper::toDocumentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        Claim claim = claimRepository.findAll().stream()
                .filter(c -> c.getDocuments().stream().anyMatch(d -> d.getId().equals(documentId)))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        // Verify ownership
        Long currentUserId = getCurrentUserId();
        if (!claim.getPolicy().getUser().getId().equals(currentUserId)) {
            throw new ClaimNotModifiableException(claim.getId(), "You can only delete your own documents");
        }

        ClaimDocument document = claim.getDocuments().stream()
                .filter(d -> d.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        // Delete physical file
        try {
            // Physical deletion logic
            Path uploadPath = Paths.get("uploads/claims"); // Base path
            Path fullPath = uploadPath.resolve(document.getFilePath());
            Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            // Log error but continue
        }

        claim.getDocuments().remove(document);
        claimRepository.save(claim);
    }

    @Transactional
    public void requestAdditionalDocuments(Long claimId, String message) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));

        // Update claim status to UNDER_REVIEW if it's SUBMITTED
        if (claim.getStatus() == ClaimStatus.SUBMITTED) {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        }

        // Store the request message in officer comments
        String currentComments = claim.getOfficerComments() != null ? claim.getOfficerComments() + "\n\n" : "";
        claim.setOfficerComments(currentComments + "[DOCUMENT REQUEST] " + message);

        claimRepository.save(claim);
    }

    private Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
    }
}
