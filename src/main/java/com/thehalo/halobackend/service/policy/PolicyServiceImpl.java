package com.thehalo.halobackend.service.policy;

import com.thehalo.halobackend.dto.policy.request.PurchasePolicyRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationResponse;
import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.domain.policy.DuplicateActivePolicyException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotActiveException;
import com.thehalo.halobackend.exception.domain.policy.ProductNotAvailableException;
import com.thehalo.halobackend.exception.domain.policy.InvalidPolicyStateException;
import com.thehalo.halobackend.exception.domain.product.ProductNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.UnauthorizedPolicyAccessException;
import com.thehalo.halobackend.mapper.policy.PolicyApplicationMapper;
import com.thehalo.halobackend.mapper.policy.PolicyMapper;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.PolicyApplicationRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.utility.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PolicyServiceImpl — Manages policy lifecycle.
 * All quote/QuoteRequest dependencies removed.
 */
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final ProductRepository productRepository;
    private final UserPlatformRepository profileRepository;
    private final PolicyApplicationRepository applicationRepository;
    private final PolicyMapper policyMapper;
    private final PolicyApplicationMapper applicationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PolicySummaryResponse> getMyPolicies() {
        return policyRepository.findByUserId(currentUserId())
                .stream().map(policyMapper::toSummaryDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicySummaryResponse> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(policyMapper::toSummaryDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyApplicationResponse> getAdminApplications() {
        return applicationRepository.findAll().stream()
                .map(applicationMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyDetailResponse getDetail(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException(policyId));
        return policyMapper.toDetailDto(policy);
    }

    @Override
    @Transactional
    public PolicyDetailResponse purchase(PurchasePolicyRequest request) {
        Long userId = currentUserId();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductNotAvailableException(request.getProductId());
        }

        UserPlatform profile = profileRepository.findByIdAndUserId(request.getProfileId(), userId)
                .orElseThrow(() -> new ProfileNotFoundException(request.getProfileId()));

        policyRepository.findByProfileIdAndProductIdAndStatus(
                profile.getId(), product.getId(), PolicyStatus.ACTIVE)
                .ifPresent(p -> {
                    throw new DuplicateActivePolicyException(profile.getId(), product.getId());
                });

        AppUser user = new AppUser();
        user.setId(userId);

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal totalLimit = nullCoalesce(product.getCoverageLimitLegal(), zero)
                .add(nullCoalesce(product.getCoverageLimitReputation(), zero))
                .add(nullCoalesce(product.getCoverageLimitCyber(), zero));

        BigDecimal riskMultiplier = BigDecimal.valueOf(profile.getPlatform().getBaseRiskFactor());
        BigDecimal dynamicPremium = product.getBasePremium().multiply(riskMultiplier);

        Policy policy = Policy.builder()
                .policyNumber(IdGeneratorUtil.generatePolicyNumber())
                .user(user)
                .profile(profile)
                .product(product)
                .totalCoverageLimit(totalLimit)
                .premiumAmount(dynamicPremium)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(PolicyStatus.PENDING_PAYMENT)
                .renewalCount(0)
                .build();

        Policy saved = policyRepository.save(policy);
        return policyMapper.toDetailDto(saved);
    }

    @Override
    @Transactional
    public PolicySummaryResponse cancel(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException(policyId));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new PolicyNotActiveException(policyId);
        }
        policy.setStatus(PolicyStatus.CANCELLED);
        Policy saved = policyRepository.save(policy);
        return policyMapper.toSummaryDto(saved);
    }

    @Override
    @Transactional
    public PolicyDetailResponse payPremium(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException(policyId));

        if (!policy.getUser().getId().equals(currentUserId())) {
            throw new UnauthorizedPolicyAccessException(policyId, currentUserId());
        }

        if (policy.getStatus() != PolicyStatus.PENDING_PAYMENT) {
            throw new InvalidPolicyStateException("Policy is not pending payment. Current status: " + policy.getStatus());
        }

        policy.setStatus(PolicyStatus.ACTIVE);
        Policy saved = policyRepository.save(policy);

        return policyMapper.toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActivePolicies() {
        Long userId = currentUserId();
        return policyRepository.existsByUserIdAndStatus(userId, PolicyStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPaidPremium() {
        Long userId = currentUserId();
        return policyRepository.existsByUserIdAndStatus(userId, PolicyStatus.ACTIVE);
    }

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }

    private <T extends BigDecimal> T nullCoalesce(T val, T fallback) {
        return val != null ? val : fallback;
    }
}
