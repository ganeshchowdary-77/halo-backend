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
import com.thehalo.halobackend.exception.domain.policy.InvalidPolicyStateException;
import com.thehalo.halobackend.exception.domain.product.ProductNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.exception.domain.quote.QuoteNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.UnauthorizedPolicyAccessException;
import com.thehalo.halobackend.mapper.policy.PolicyMapper;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
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
        private final UserPlatformRepository profileRepository;
        private final QuoteRequestRepository quoteRepository;
        private final PolicyMapper policyMapper;
        private final com.thehalo.halobackend.mapper.quote.QuoteMapper quoteMapper;

        // List all policies owned by the current user — lightweight summary cards
        @Transactional(readOnly = true)
        public List<PolicySummaryResponse> getMyPolicies() {
                return policyRepository.findByUserId(currentUserId())
                                .stream().map(policyMapper::toSummaryDto).toList();
        }

        @Transactional(readOnly = true)
        public List<PolicySummaryResponse> getAllPolicies() {
                return policyRepository.findAll().stream()
                                .map(policyMapper::toSummaryDto).toList();
        }

        @Transactional(readOnly = true)
        public List<com.thehalo.halobackend.dto.policy.response.PolicyApplicationResponse> getAdminApplications() {
                // Return all quote requests as logs for the admin to monitor
                return quoteRepository.findAll().stream()
                                .map(quote -> {
                                        var dto = quoteMapper.toApplicationDto(quote);
                                        
                                        // If quote was converted to policy, fetch the policy status
                                        if (quote.getStatus() == QuoteStatus.ACCEPTED || 
                                            quote.getStatus() == QuoteStatus.CONVERTED_TO_POLICY) {
                                                policyRepository.findByProfileIdAndProductId(
                                                        quote.getProfile().getId(), 
                                                        quote.getProduct().getId()
                                                ).stream()
                                                .findFirst()
                                                .ifPresent(policy -> {
                                                        dto.setPolicyStatus(policy.getStatus());
                                                        // Use policy's risk score if available, otherwise use quote's
                                                        if (policy.getRiskScore() != null) {
                                                                dto.setRiskScore(policy.getRiskScore());
                                                        }
                                                });
                                        }
                                        
                                        return dto;
                                }).toList();
        }

        @Transactional
        public void approvePolicyApplication(Long id) {
                // No longer used by admin UI but keeping service method for internal auto-approval if needed
        }

        @Transactional
        public void rejectPolicyApplication(Long id, String reason) {
                // No longer used by admin UI
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
                                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

                if (!Boolean.TRUE.equals(product.getActive())) {
                        throw new ProductNotAvailableException(request.getProductId());
                }

                UserPlatform profile = profileRepository.findByIdAndUserId(request.getProfileId(), userId)
                                .orElseThrow(() -> new ProfileNotFoundException(request.getProfileId()));

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
                                .status(PolicyStatus.PENDING_PAYMENT)
                                .renewalCount(0)
                                .build();

                Policy saved = policyRepository.save(policy);
                return policyMapper.toDetailDto(saved);
        }

        // Purchase an active policy directly from an APPROVED underwriter quote
        @Transactional
        public PolicyDetailResponse purchaseFromQuote(Long quoteId) {
                QuoteRequest quote = quoteRepository.findById(quoteId)
                                .orElseThrow(() -> new QuoteNotFoundException(quoteId));

                if (!quote.getUser().getId().equals(currentUserId())) {
                        throw new UnauthorizedPolicyAccessException(quoteId, currentUserId());
                }
                if (quote.getStatus() != QuoteStatus.APPROVED) {
                        throw new InvalidPolicyStateException(
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
                                .status(PolicyStatus.PENDING_PAYMENT)
                                .renewalCount(0)
                                // Store underwriter who quoted it
                                .underwriter(quote.getAssignedUnderwriter())
                                .build();

                Policy saved = policyRepository.save(policy);
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
                return policyMapper.toSummaryDto(saved);
        }

        // Pay premium to activate a pending policy
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

        private Long currentUserId() {
                return ((CustomUserDetails) SecurityContextHolder.getContext()
                                .getAuthentication().getPrincipal()).getUserId();
        }

        private <T extends java.math.BigDecimal> T nullCoalesce(T val, T fallback) {
                return val != null ? val : fallback;
        }

        // Helper methods for navigation visibility
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
                // Check if user has any policy with ACTIVE status (which means premium was paid)
                return policyRepository.existsByUserIdAndStatus(userId, PolicyStatus.ACTIVE);
        }

}
