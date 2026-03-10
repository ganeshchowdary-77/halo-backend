package com.thehalo.halobackend.service.claim;

import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.request.ReviewClaimRequest;
import com.thehalo.halobackend.dto.claim.response.*;
import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.exception.domain.claim.*;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotActiveException;
import com.thehalo.halobackend.mapper.claim.ClaimMapper;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.claim.ClaimTimeline;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.repository.ClaimRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.UserProfileRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.service.system.AuditLogService;
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
    private final UserProfileRepository profileRepository;
    private final AuditLogService auditLogService;
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
        Claim claim = findOrThrow(claimId);
        return claimMapper.toDetailDto(claim);
    }

    // Influencer: file a new defamation claim
    @Transactional
    public ClaimDetailResponse file(FileClaimRequest request) {
        Long userId = currentUserId();

        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new RuntimeException("Policy not found: " + request.getPolicyId()));

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

        var profile = profileRepository.findByIdAndUserId(request.getProfileId(), userId)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + request.getProfileId()));

        AppUser filer = new AppUser();
        filer.setId(userId);

        Claim claim = Claim.builder()
                .claimNumber(IdGeneratorUtil.generateClaimNumber())
                .policy(policy)
                .profile(profile)
                .filedBy(filer)
                .incidentDate(request.getIncidentDate())
                .description(request.getDescription())
                .incidentUrl(request.getIncidentUrl())
                .expenseType(request.getExpenseType())
                .claimAmount(request.getClaimAmount())
                .status(ClaimStatus.SUBMITTED)
                .build();

        Claim saved = claimRepository.save(claim);

        // Add initial timeline entry
        addTimeline(saved, null, ClaimStatus.SUBMITTED, "Claim filed by influencer", filer);
        auditLogService.logAction("CLAIM", saved.getId().toString(), "CREATE",
                "Filed new claim for amount: " + request.getClaimAmount());
        return claimMapper.toDetailDto(saved);
    }

    // Claims Officer: approve a pending claim
    @Transactional
    public ClaimDetailResponse approve(Long claimId, ReviewClaimRequest request) {
        Claim claim = findOrThrow(claimId);
        guardModifiable(claim);

        AppUser officer = officerUser();
        addTimeline(claim, claim.getStatus(), ClaimStatus.APPROVED, request.getOfficerComments(), officer);

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAmount(request.getApprovedAmount());
        claim.setOfficerComments(request.getOfficerComments());
        claim.setAssignedOfficer(officer);
        claim.setReviewedAt(LocalDateTime.now());

        Claim saved = claimRepository.save(claim);
        auditLogService.logAction("CLAIM", saved.getId().toString(), "APPROVE",
                "Approved claim for amount: " + request.getApprovedAmount());
        return claimMapper.toDetailDto(saved);
    }

    // Claims Officer: deny a pending claim
    @Transactional
    public ClaimDetailResponse deny(Long claimId, ReviewClaimRequest request) {
        Claim claim = findOrThrow(claimId);
        guardModifiable(claim);

        AppUser officer = officerUser();
        addTimeline(claim, claim.getStatus(), ClaimStatus.DENIED, request.getOfficerComments(), officer);

        claim.setStatus(ClaimStatus.DENIED);
        claim.setOfficerComments(request.getOfficerComments());
        claim.setAssignedOfficer(officer);
        claim.setReviewedAt(LocalDateTime.now());

        Claim saved = claimRepository.save(claim);
        auditLogService.logAction("CLAIM", saved.getId().toString(), "DENY",
                "Denied claim. Reason: " + request.getOfficerComments());
        return claimMapper.toDetailDto(saved);
    }

    // Claims Officer: queue of all submitted claims awaiting review
    @Transactional(readOnly = true)
    public List<ClaimSummaryResponse> getClaimQueue() {
        return claimRepository.findByStatus(ClaimStatus.SUBMITTED)
                .stream().map(claimMapper::toSummaryDto).toList();
    }

    private void addTimeline(Claim claim, ClaimStatus from, ClaimStatus to, String note, AppUser by) {
        claim.getTimeline().add(ClaimTimeline.builder()
                .claim(claim).fromStatus(from).toStatus(to)
                .note(note).changedBy(by).build());
    }

    private void guardModifiable(Claim claim) {
        if (claim.getStatus() != ClaimStatus.SUBMITTED && claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new ClaimNotModifiableException(claim.getId(), claim.getStatus().name());
        }
    }

    private Claim findOrThrow(Long id) {
        return claimRepository.findById(id)
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

}
