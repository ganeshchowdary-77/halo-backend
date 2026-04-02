package com.thehalo.halobackend.service.policy;

import com.thehalo.halobackend.dto.policy.request.SubmitPolicyApplicationRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.domain.policy.ApplicationNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.DuplicateActivePolicyException;
import com.thehalo.halobackend.exception.domain.policy.InvalidApplicationStateException;
import com.thehalo.halobackend.exception.domain.product.ProductNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.mapper.policy.PolicyApplicationMapper;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.PolicyApplication;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.repository.PolicyApplicationRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.service.underwriting.RiskPricingService;
import com.thehalo.halobackend.utility.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyApplicationServiceImpl implements PolicyApplicationService {

    private final PolicyApplicationRepository applicationRepository;
    private final PolicyRepository policyRepository;
    private final ProductRepository productRepository;
    private final UserPlatformRepository profileRepository;
    private final AppUserRepository userRepository;
    private final RiskPricingService riskPricingService;
    private final PolicyApplicationMapper applicationMapper;

    @Override
    @Transactional
    public PolicyApplicationDetailResponse submitApplication(SubmitPolicyApplicationRequest request) {
        Long userId = currentUserId();

        // 1. Validate product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new com.thehalo.halobackend.exception.domain.policy.ProductNotAvailableException(request.getProductId());
        }

        // 2. Validate ownership of profile
        UserPlatform profile = profileRepository.findByIdAndUserId(request.getProfileId(), userId)
                .orElseThrow(() -> new ProfileNotFoundException(request.getProfileId()));

        // 3. Check for duplicate active application or policy
        long existingActive = applicationRepository.countActiveApplicationsForProfileAndProduct(
                userId, profile.getId(), product.getId());
        if (existingActive > 0) {
            throw new DuplicateActivePolicyException(profile.getId(), product.getId());
        }
        policyRepository.findByProfileIdAndProductIdAndStatus(
                profile.getId(), product.getId(), PolicyStatus.ACTIVE)
                .ifPresent(p -> {
                    throw new DuplicateActivePolicyException(profile.getId(), product.getId());
                });

        // 4. Build the application entity
        AppUser user = new AppUser();
        user.setId(userId);

        PolicyApplication application = PolicyApplication.builder()
                .applicationNumber(IdGeneratorUtil.generateApplicationNumber())
                .user(user)
                .profile(profile)
                .product(product)
                .notes(request.getNotes())
                .hasTwoFactorAuth(request.getHasTwoFactorAuth())
                .passwordRotationFrequency(request.getPasswordRotationFrequency())
                .thirdPartyManagement(request.getThirdPartyManagement())
                .sponsoredContentFrequency(request.getSponsoredContentFrequency())
                .build();

        // 5. Auto-calculate risk and premium
        int riskScore = riskPricingService.calculateRiskScore(profile, product, application);
        BigDecimal calculatedPremium = riskPricingService.calculatePersonalizedPremium(product, profile, application);
        boolean requiresReview = riskPricingService.requiresUnderwriterReview(product, profile, application);

        application.setRiskScore(riskScore);
        application.setCalculatedPremium(calculatedPremium);
        application.setRequiresReview(requiresReview);

        if (!requiresReview) {
            // 6a. AUTO-APPROVED: Create policy immediately
            application.setStatus(PolicyStatus.PENDING_PAYMENT);
            PolicyApplication saved = applicationRepository.save(application);

            Policy policy = createPolicyFromApplication(saved, profile, product, calculatedPremium);
            saved.setPolicyId(policy.getId());
            applicationRepository.save(saved);

            log.info("Application {} auto-approved. Policy {} created with premium ${}",
                    saved.getApplicationNumber(), policy.getPolicyNumber(), calculatedPremium);

            return applicationMapper.toDetailDto(saved);
        } else {
            // 6b. HIGH-RISK: Auto-assign to underwriter
            application.setStatus(PolicyStatus.UNDER_REVIEW);

            // Round-robin assignment to least-busy underwriter
            List<AppUser> underwriters = userRepository.findActiveUnderwritersOrderByWorkload();
            if (!underwriters.isEmpty()) {
                application.setAssignedUnderwriter(underwriters.get(0));
                log.info("Application {} assigned to underwriter {} for review (riskScore={})",
                        application.getApplicationNumber(), underwriters.get(0).getFullName(), riskScore);
            } else {
                log.warn("No available underwriters for high-risk application {}", application.getApplicationNumber());
            }

            PolicyApplication saved = applicationRepository.save(application);
            return applicationMapper.toDetailDto(saved);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyApplicationDetailResponse> getMyApplications() {
        Long userId = currentUserId();
        return applicationRepository.findByUserId(userId).stream()
                .map(applicationMapper::toDetailDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyApplicationDetailResponse getApplicationDetail(Long applicationId) {
        PolicyApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        return applicationMapper.toDetailDto(app);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyApplicationDetailResponse> getAssignedApplications(Long underwriterId) {
        return applicationRepository.findByAssignedUnderwriterId(underwriterId).stream()
                .map(applicationMapper::toDetailDto)
                .toList();
    }

    @Override
    @Transactional
    public PolicyApplicationDetailResponse approveApplication(Long applicationId, String underwriterNotes) {
        PolicyApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (app.getStatus() != PolicyStatus.UNDER_REVIEW) {
            throw new InvalidApplicationStateException(
                    "Application must be UNDER_REVIEW to approve. Current: " + app.getStatus());
        }

        app.setStatus(PolicyStatus.PENDING_PAYMENT);
        app.setUnderwriterNotes(underwriterNotes);
        app.setReviewedAt(LocalDateTime.now());

        Policy policy = createPolicyFromApplication(app, app.getProfile(), app.getProduct(), app.getCalculatedPremium());
        app.setPolicyId(policy.getId());

        PolicyApplication saved = applicationRepository.save(app);

        log.info("Application {} approved by underwriter. Policy {} created.",
                saved.getApplicationNumber(), policy.getPolicyNumber());

        return applicationMapper.toDetailDto(saved);
    }

    @Override
    @Transactional
    public PolicyApplicationDetailResponse rejectApplication(Long applicationId, String reason) {
        PolicyApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (app.getStatus() != PolicyStatus.UNDER_REVIEW) {
            throw new InvalidApplicationStateException(
                    "Application must be UNDER_REVIEW to reject. Current: " + app.getStatus());
        }

        app.setStatus(PolicyStatus.APPLICATION_REJECTED);
        app.setUnderwriterNotes(reason);
        app.setReviewedAt(LocalDateTime.now());

        PolicyApplication saved = applicationRepository.save(app);
        log.info("Application {} rejected by underwriter. Reason: {}", saved.getApplicationNumber(), reason);

        return applicationMapper.toDetailDto(saved);
    }

    // ── Helper: Create Policy from approved application ──

    private Policy createPolicyFromApplication(PolicyApplication app, UserPlatform profile, Product product, BigDecimal premium) {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal totalLimit = nullCoalesce(product.getCoverageLimitLegal(), zero)
                .add(nullCoalesce(product.getCoverageLimitReputation(), zero))
                .add(nullCoalesce(product.getCoverageLimitCyber(), zero));

        Policy policy = Policy.builder()
                .policyNumber(IdGeneratorUtil.generatePolicyNumber())
                .user(app.getUser())
                .profile(profile)
                .product(product)
                .totalCoverageLimit(totalLimit)
                .premiumAmount(premium)
                .riskScore(app.getRiskScore())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .status(PolicyStatus.PENDING_PAYMENT)
                .renewalCount(0)
                .underwriter(app.getAssignedUnderwriter())
                .build();

        return policyRepository.save(policy);
    }

    private <T extends BigDecimal> T nullCoalesce(T val, T fallback) {
        return val != null ? val : fallback;
    }

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }
}
