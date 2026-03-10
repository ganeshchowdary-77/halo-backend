package com.thehalo.halobackend.service.policy;

import com.thehalo.halobackend.dto.policy.request.PurchasePolicyRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.exception.domain.policy.DuplicateActivePolicyException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotActiveException;
import com.thehalo.halobackend.exception.domain.policy.ProductNotAvailableException;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.mapper.policy.PolicyMapper;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.model.profile.UserProfile;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.UserProfileRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.service.system.AuditLogService;
import com.thehalo.halobackend.utility.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Manages insurance policy purchase, cancellation, and retrieval
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

        private final PolicyRepository policyRepository;
        private final ProductRepository productRepository;
        private final UserProfileRepository profileRepository;
        private final QuoteRequestRepository quoteRepository;
        private final AuditLogService auditLogService;
        private final PolicyMapper policyMapper;

        // List all policies owned by the current user — lightweight summary cards
        @Transactional(readOnly = true)
        public List<PolicySummaryResponse> getMyPolicies() {
                return policyRepository.findByUserId(currentUserId())
                                .stream().map(policyMapper::toSummaryDto).toList();
        }

        // Full detail of a specific policy — accessible by owner or admin
        @Transactional(readOnly = true)
        public PolicyDetailResponse getDetail(Long policyId) {
                Policy policy = policyRepository.findById(policyId)
                                .orElseThrow(() -> new PolicyNotFoundException(policyId));
                return policyMapper.toDetailDto(policy);
        }

        // Purchase a new policy for a given profile and product tier directly
        @Transactional
        public PolicyDetailResponse purchase(PurchasePolicyRequest request) {
                Long userId = currentUserId();
                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found: " + request.getProductId()));

                if (!Boolean.TRUE.equals(product.getActive())) {
                        throw new ProductNotAvailableException(request.getProductId());
                }

                UserProfile profile = profileRepository.findByIdAndUserId(request.getProfileId(), userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Profile not found: " + request.getProfileId()));

                // Prevent duplicate active policy for same profile+product combination
                policyRepository.findByProfileIdAndProductIdAndStatus(
                                profile.getId(), product.getId(), PolicyStatus.ACTIVE)
                                .ifPresent(p -> {
                                        throw new DuplicateActivePolicyException(profile.getId(), product.getId());
                                });

                AppUser user = new AppUser();
                user.setId(userId);

                // Compute total coverage limit from all sub-limits
                var zero = java.math.BigDecimal.ZERO;
                var totalLimit = nullCoalesce(product.getCoverageLimitLegal(), zero)
                                .add(nullCoalesce(product.getCoverageLimitPR(), zero))
                                .add(nullCoalesce(product.getCoverageLimitMonitoring(), zero));

                // Calculate dynamic premium based on platform base risk factor
                java.math.BigDecimal riskMultiplier = java.math.BigDecimal
                                .valueOf(profile.getPlatform().getBaseRiskFactor());
                java.math.BigDecimal dynamicPremium = product.getBasePremium().multiply(riskMultiplier);

                Policy policy = Policy.builder()
                                .policyNumber(IdGeneratorUtil.generatePolicyNumber())
                                .user(user)
                                .profile(profile)
                                .product(product)
                                .totalCoverageLimit(totalLimit)
                                .premiumAmount(dynamicPremium)
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusYears(1))
                                .status(PolicyStatus.ACTIVE)
                                .renewalCount(0)
                                .build();

                Policy saved = policyRepository.save(policy);
                auditLogService.logAction("POLICY", saved.getId().toString(), "CREATE",
                                "Purchased new policy directly for product: " + product.getName());
                return policyMapper.toDetailDto(saved);
        }

        // Purchase an active policy directly from an APPROVED underwriter quote
        @Transactional
        public PolicyDetailResponse purchaseFromQuote(Long quoteId) {
                QuoteRequest quote = quoteRepository.findById(quoteId)
                                .orElseThrow(() -> new ResourceNotFoundException("Quote not found: " + quoteId));

                if (!quote.getUser().getId().equals(currentUserId())) {
                        throw new RuntimeException("Quote belongs to another user");
                }
                if (quote.getStatus() != QuoteStatus.APPROVED) {
                        throw new RuntimeException(
                                        "Quote must be APPROVED to purchase. Current status: "
                                                        + quote.getStatus().name());
                }

                // Check duplicates
                policyRepository.findByProfileIdAndProductIdAndStatus(
                                quote.getProfile().getId(), quote.getProduct().getId(), PolicyStatus.ACTIVE)
                                .ifPresent(p -> {
                                        throw new DuplicateActivePolicyException(quote.getProfile().getId(),
                                                        quote.getProduct().getId());
                                });

                quote.setStatus(QuoteStatus.ACCEPTED);
                quoteRepository.save(quote);

                Product product = quote.getProduct();
                var zero = java.math.BigDecimal.ZERO;
                var totalLimit = nullCoalesce(product.getCoverageLimitLegal(), zero)
                                .add(nullCoalesce(product.getCoverageLimitPR(), zero))
                                .add(nullCoalesce(product.getCoverageLimitMonitoring(), zero));

                Policy policy = Policy.builder()
                                .policyNumber(IdGeneratorUtil.generatePolicyNumber())
                                .user(quote.getUser())
                                .profile(quote.getProfile())
                                .product(product)
                                .totalCoverageLimit(totalLimit)
                                // Use the underwriter's custom premium instead of base
                                .premiumAmount(quote.getOfferedPremium())
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusYears(1))
                                .status(PolicyStatus.ACTIVE)
                                .renewalCount(0)
                                // Store underwriter who quoted it
                                .underwriter(quote.getAssignedUnderwriter())
                                .build();

                Policy saved = policyRepository.save(policy);
                auditLogService.logAction("POLICY", saved.getId().toString(), "CREATE",
                                "Purchased policy from approved quote for product: " + product.getName());
                return policyMapper.toDetailDto(saved);
        }

        // Cancel an active policy — only the owner can cancel
        @Transactional
        public PolicySummaryResponse cancel(Long policyId) {
                Policy policy = policyRepository.findById(policyId)
                                .orElseThrow(() -> new PolicyNotFoundException(policyId));

                if (policy.getStatus() != PolicyStatus.ACTIVE) {
                        throw new PolicyNotActiveException(policyId);
                }
                policy.setStatus(PolicyStatus.CANCELLED);
                Policy saved = policyRepository.save(policy);
                auditLogService.logAction("POLICY", saved.getId().toString(), "CANCEL",
                                "Cancelled policy: " + saved.getPolicyNumber());
                return policyMapper.toSummaryDto(saved);
        }

        private Long currentUserId() {
                return ((CustomUserDetails) SecurityContextHolder.getContext()
                                .getAuthentication().getPrincipal()).getUserId();
        }

        private <T extends java.math.BigDecimal> T nullCoalesce(T val, T fallback) {
                return val != null ? val : fallback;
        }

}
