package com.thehalo.halobackend.service.claim;

import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.request.ReviewClaimRequest;
import com.thehalo.halobackend.dto.claim.response.*;
import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.exception.business.BusinessRuleViolationException;
import com.thehalo.halobackend.exception.domain.claim.*;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotActiveException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.UnauthorizedPolicyAccessException;
import com.thehalo.halobackend.mapper.claim.ClaimMapper;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.ClaimRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.utility.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Handles claim filing, retrieval, and officer review workflow
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final UserPlatformRepository profileRepository;
    private final ClaimDocumentService claimDocumentService;

    private final ClaimMapper claimMapper;
    // Influencer: list their own claims
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> getMyClaims() {
        return claimRepository.findByFiledById(currentUserId())
                .stream().map(claimMapper::toSummaryDto).toList();
    }

    // Influencer or officer: full detail of a single claim
    @Transactional(readOnly = true)
    public ClaimDetailResponse getDetail(Long claimId) {
        Claim claim = claimRepository.findByIdWithDocuments(claimId)
                .orElseThrow(() -> new ClaimNotFoundException(claimId));
        ClaimDetailResponse response = claimMapper.toDetailDto(claim);
        
        // Manually map documents to avoid MapStruct applying URL transformation to all fields
        if (claim.getDocuments() != null && !claim.getDocuments().isEmpty()) {
            java.util.List<ClaimDocumentResponse> docs = claim.getDocuments().stream()
                    .map(claimMapper::toDocumentDto)
                    .collect(java.util.stream.Collectors.toList());
            response.setDocuments(docs);
        }
        
        // Debug logging
        System.out.println("=== CLAIM DETAIL DEBUG ===");
        System.out.println("Claim Number: " + response.getClaimNumber());
        System.out.println("Description: " + response.getDescription());
        System.out.println("Profile Platform: " + response.getProfilePlatform());
        System.out.println("Profile Handle: " + response.getProfileHandle());
        if (response.getDocuments() != null && !response.getDocuments().isEmpty()) {
            System.out.println("First Document FileName: " + response.getDocuments().get(0).getFileName());
            System.out.println("First Document URL: " + response.getDocuments().get(0).getDocumentUrl());
        }
        System.out.println("========================");
        
        return response;
    }

    private final com.thehalo.halobackend.service.common.FileStorageService fileStorageService;

    // Influencer: file a new defamation claim
    @Transactional
    public ClaimDetailResponse file(FileClaimRequest request, List<org.springframework.web.multipart.MultipartFile> documents) {
        Long userId = currentUserId();

        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new PolicyNotFoundException(request.getPolicyId()));

        // Claim can only be filed against ACTIVE policies
        if (policy.getStatus() != com.thehalo.halobackend.enums.PolicyStatus.ACTIVE) {
            throw new PolicyNotActiveException(request.getPolicyId());
        }

        // Claim amount must not exceed total coverage limit
        if (request.getClaimAmount().compareTo(policy.getTotalCoverageLimit()) > 0) {
            throw new ClaimAmountExceedsCoverageException(
                    request.getClaimAmount().doubleValue(),
                    policy.getTotalCoverageLimit().doubleValue());
        }

        // Get the UserPlatform from the policy
        UserPlatform userPlatform = policy.getProfile();
        if (userPlatform == null) {
            userPlatform = profileRepository.findByUserId(userId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleViolationException("User has no platforms to file claim against"));
        }

        if (!userPlatform.getUser().getId().equals(userId)) {
            throw new UnauthorizedPolicyAccessException(policy.getId(), userId);
        }

        AppUser filer = new AppUser();
        filer.setId(userId);

        Claim claim = Claim.builder()
                .claimNumber(IdGeneratorUtil.generateClaimNumber())
                .policy(policy)
                .profile(userPlatform)
                .filedBy(filer)
                .incidentDate(request.getIncidentDate())
                .description(request.getDescription())
                .incidentUrl(request.getIncidentUrl())
                .expenseType(request.getExpenseType())
                .claimAmount(request.getClaimAmount())
                .status(ClaimStatus.SUBMITTED)
                .build();

        // Process and store documents
        if (documents != null && !documents.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile file : documents) {
                if (!file.isEmpty()) {
                    String path = fileStorageService.storeFile(file, "claims", userId);
                    com.thehalo.halobackend.model.claim.ClaimDocument doc = com.thehalo.halobackend.model.claim.ClaimDocument.builder()
                            .claim(claim)
                            .fileName(file.getOriginalFilename())
                            .filePath(path)
                            .documentType("OTHER") // Default category
                            .fileSizeBytes(file.getSize())
                            .build();
                    claim.getDocuments().add(doc);
                }
            }
        }

        Claim saved = claimRepository.save(claim);
        return claimMapper.toDetailDto(saved);
    }

    // Claims Officer: approve a pending claim
    @Transactional
    public ClaimDetailResponse approve(Long claimId, ReviewClaimRequest request) {
        Claim claim = findOrThrow(claimId);
        guardModifiable(claim);

        AppUser officer = officerUser();

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAmount(request.getApprovedAmount());
        claim.setOfficerComments(request.getOfficerComments());
        claim.setAssignedOfficer(officer);
        claim.setReviewedAt(LocalDateTime.now());

        Claim saved = claimRepository.save(claim);
        return claimMapper.toDetailDto(saved);
    }

    // Claims Officer: deny a pending claim
    @Transactional
    public ClaimDetailResponse deny(Long claimId, ReviewClaimRequest request) {
        Claim claim = findOrThrow(claimId);
        guardModifiable(claim);

        AppUser officer = officerUser();

        claim.setStatus(ClaimStatus.DENIED);
        claim.setOfficerComments(request.getOfficerComments());
        claim.setAssignedOfficer(officer);
        claim.setReviewedAt(LocalDateTime.now());

        Claim saved = claimRepository.save(claim);
        return claimMapper.toDetailDto(saved);
    }

    // Officer: request more documents
    @Transactional
    public ClaimDetailResponse requestDocuments(Long claimId, ReviewClaimRequest request) {
        Claim claim = findOrThrow(claimId);
        
        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new ClaimNotModifiableException(claimId, "Only UNDER_REVIEW claims can be marked for more documents");
        }

        claim.setStatus(ClaimStatus.PENDING_INFORMATION);
        claim.setOfficerComments(request.getOfficerComments());
        
        Claim saved = claimRepository.save(claim);
        return claimMapper.toDetailDto(saved);
    }

    // Influencer: append documents
    @Transactional
    public ClaimDetailResponse uploadAdditionalDocuments(Long claimId, List<org.springframework.web.multipart.MultipartFile> documents) {
        Claim claim = findOrThrow(claimId);
        
        if (claim.getStatus() != ClaimStatus.PENDING_INFORMATION && claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new ClaimNotModifiableException(claimId, "Can only append documents to pending or submitted claims");
        }

        Long currentUserId = currentUserId();
        if (!claim.getFiledBy().getId().equals(currentUserId)) {
            throw new com.thehalo.halobackend.exception.business.BusinessRuleViolationException(
                    "You are not authorized to append documents to this claim"
            );
        }

        if (documents != null && !documents.isEmpty()) {
            documents.forEach(file -> claimDocumentService.uploadDocument(claimId, file, "OTHER"));
        }

        // Return the claim to UNDER_REVIEW so the officer knows it was updated
        if (claim.getStatus() == ClaimStatus.PENDING_INFORMATION) {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
            claim.setOfficerComments(null); // Clear the request note
        }

        Claim saved = claimRepository.save(claim);
        return claimMapper.toDetailDto(saved);
    }

    // Claims Officer: queue of all submitted claims awaiting review
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> getClaimQueue() {
        return claimRepository.findByStatus(ClaimStatus.SUBMITTED)
                .stream().map(claimMapper::toSummaryDto).toList();
    }

    @Override
        @Transactional(readOnly = true)
        public org.springframework.data.domain.Page<ClaimSummaryResponse> getClaimQueuePaginated(String search, ClaimStatus status, org.springframework.data.domain.Pageable pageable) {
            String searchPattern = (search == null) ? "" : search;
            // Default to SUBMITTED status if no status is provided - queue should only show unassigned submitted claims
            ClaimStatus filterStatus = (status == null) ? ClaimStatus.SUBMITTED : status;
            return claimRepository.findBySearchAndStatus(searchPattern, filterStatus, pageable)
                    .map(claimMapper::toSummaryDto);
        }


    // Public: get all approved claims for settlement logs
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> getApprovedClaims() {
        return claimRepository.findByStatus(ClaimStatus.APPROVED)
                .stream().map(claimMapper::toSummaryDto).toList();
    }
    
    // Officer: assign claim to current officer
    @Transactional
    public ClaimDetailResponse assignClaim(Long claimId) {
        Claim claim = findOrThrow(claimId);
        
        // Only allow assignment of SUBMITTED claims
        if (claim.getStatus() != ClaimStatus.SUBMITTED) {
            throw new ClaimNotModifiableException(claimId, "Only SUBMITTED claims can be assigned");
        }
        
        AppUser officer = officerUser();
        
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setAssignedOfficer(officer);
        
        Claim saved = claimRepository.save(claim);
        return claimMapper.toDetailDto(saved);
    }
    
    // Officer: release claim back to queue
    @Transactional
    public ClaimDetailResponse releaseClaim(Long claimId) {
        Claim claim = findOrThrow(claimId);
        
        // Only allow releasing of UNDER_REVIEW claims
        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new ClaimNotModifiableException(claimId, "Only UNDER_REVIEW claims can be released");
        }
        
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setAssignedOfficer(null);
        
        Claim saved = claimRepository.save(claim);
        return claimMapper.toDetailDto(saved);
    }
    
    // Officer: get claims assigned to current officer
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> getAssignedClaims() {
        Long officerId = currentUserId();
        return claimRepository.findByAssignedOfficerId(officerId)
                .stream().map(claimMapper::toSummaryDto).toList();
    }

    private void guardModifiable(Claim claim) {
        if (claim.getStatus() != ClaimStatus.SUBMITTED && claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new ClaimNotModifiableException(claim.getId(), claim.getStatus().name());
        }
    }

    private Claim findOrThrow(Long id) {
        return claimRepository.findByIdWithDocuments(id)
                .orElseThrow(() -> new ClaimNotFoundException(id));
    }

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }

    private AppUser officerUser() {
        AppUser officer = new AppUser();
        officer.setId(currentUserId());
        return officer;
    }

    // Search claims by user name (for Claims Officers)
    @Override
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> searchClaimsByUserName(String userName) {
        return claimRepository.findByUserName(userName)
                .stream()
                .map(claimMapper::toSummaryDto)
                .toList();
    }

    // Get user claim history (for Claims Officers)
    @Override
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> getUserClaimHistory(Long userId) {
        return claimRepository.findByFiledById(userId)
                .stream()
                .map(claimMapper::toSummaryDto)
                .toList();
    }

}
