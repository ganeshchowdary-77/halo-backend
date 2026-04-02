package com.thehalo.halobackend.service.dashboard;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.domain.policy.ApplicationNotFoundException;
import com.thehalo.halobackend.model.policy.PolicyApplication;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.PolicyApplicationRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnderwriterDashboardServiceImpl implements UnderwriterDashboardService {

    private final PolicyApplicationRepository applicationRepository;
    private final PolicyRepository policyRepository;

    @Override
    public Map<String, Object> getOverview() {
        AppUser currentUser = getCurrentUser();

        long totalApplications = applicationRepository.count();
        long underReview = applicationRepository.countByStatus(PolicyStatus.UNDER_REVIEW);
        long approved = applicationRepository.countByStatus(PolicyStatus.PENDING_PAYMENT);
        long rejected = applicationRepository.countByStatus(PolicyStatus.APPLICATION_REJECTED);
        long activePolicies = policyRepository.count();

        // Assigned to this underwriter
        int myAssigned = applicationRepository.countActiveApplicationsByUnderwriter(currentUser.getId());

        // Calculate total premium volume from all approved applications
        BigDecimal totalPremiumVolume = applicationRepository.findByStatus(PolicyStatus.PENDING_PAYMENT)
            .stream()
            .map(PolicyApplication::getCalculatedPremium)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate average premium
        BigDecimal avgPremium = approved > 0 ?
            totalPremiumVolume.divide(BigDecimal.valueOf(approved), 2, java.math.RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalApplications", totalApplications);
        overview.put("underReview", underReview);
        overview.put("approved", approved);
        overview.put("rejected", rejected);
        overview.put("activePolicies", activePolicies);
        overview.put("totalPremiumVolume", totalPremiumVolume);
        overview.put("avgPremium", avgPremium);
        overview.put("conversionRate", totalApplications > 0 ? (approved * 100.0 / totalApplications) : 0);

        // Personal Metrics
        overview.put("myAssignedApplications", myAssigned);

        return overview;
    }

    private AppUser getCurrentUser() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new IllegalStateException("User not authenticated");
    }

    @Override
    public Map<String, Object> getPremiumCalculations(int page, int size) {
        Page<PolicyApplication> appsPage = applicationRepository.findAll(PageRequest.of(page, size));

        List<Map<String, Object>> calculations = appsPage.getContent().stream()
            .map(this::mapApplicationToPremiumCalculation)
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("calculations", calculations);
        result.put("currentPage", page);
        result.put("totalPages", appsPage.getTotalPages());
        result.put("totalElements", appsPage.getTotalElements());

        return result;
    }

    @Override
    public Map<String, Object> getPremiumCalculationDetail(Long applicationId) {
        PolicyApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        return buildDetailedCalculation(application);
    }

    private Map<String, Object> mapApplicationToPremiumCalculation(PolicyApplication application) {
        Map<String, Object> calc = new HashMap<>();
        calc.put("applicationId", application.getId());
        calc.put("applicationNumber", application.getApplicationNumber());
        calc.put("userEmail", application.getUser().getEmail());
        calc.put("userName", application.getUser().getFullName());
        calc.put("platform", application.getProfile().getPlatform().getName().name());
        calc.put("handle", application.getProfile().getHandle());
        calc.put("followerCount", application.getProfile().getFollowerCount());
        calc.put("niche", application.getProfile().getNiche().name());
        calc.put("productName", application.getProduct().getName());
        calc.put("calculatedPremium", application.getCalculatedPremium());
        calc.put("riskScore", application.getRiskScore());
        calc.put("status", application.getStatus().name());
        calc.put("createdAt", application.getCreatedAt());

        // Check if converted to policy
        if (application.getPolicyId() != null) {
            policyRepository.findById(application.getPolicyId()).ifPresent(policy -> {
                calc.put("convertedToPolicy", true);
                calc.put("policyNumber", policy.getPolicyNumber());
                calc.put("policyStatus", policy.getStatus().name());
                calc.put("totalPremiumPaid", policy.getTotalPremiumPaid());
            });
        } else {
            calc.put("convertedToPolicy", false);
        }

        return calc;
    }

    private Map<String, Object> buildDetailedCalculation(PolicyApplication application) {
        Map<String, Object> detail = new HashMap<>();

        detail.put("applicationId", application.getId());
        detail.put("applicationNumber", application.getApplicationNumber());
        detail.put("status", application.getStatus().name());
        detail.put("createdAt", application.getCreatedAt());
        detail.put("riskScore", application.getRiskScore());

        // User info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", application.getUser().getEmail());
        userInfo.put("fullName", application.getUser().getFullName());
        detail.put("user", userInfo);

        // Profile info
        Map<String, Object> profileInfo = new HashMap<>();
        profileInfo.put("platform", application.getProfile().getPlatform().getName().name());
        profileInfo.put("handle", application.getProfile().getHandle());
        profileInfo.put("followerCount", application.getProfile().getFollowerCount());
        profileInfo.put("engagementRate", application.getProfile().getEngagementRate());
        profileInfo.put("niche", application.getProfile().getNiche().name());
        profileInfo.put("verified", application.getProfile().getVerified());
        detail.put("profile", profileInfo);

        // Product info
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("name", application.getProduct().getName());
        productInfo.put("basePremium", application.getProduct().getBasePremium());
        productInfo.put("coverageAmount", application.getProduct().getCoverageAmount());
        detail.put("product", productInfo);

        // Security assessment
        Map<String, Object> security = new HashMap<>();
        security.put("hasTwoFactorAuth", application.getHasTwoFactorAuth());
        security.put("passwordRotationFrequency", application.getPasswordRotationFrequency());
        security.put("thirdPartyManagement", application.getThirdPartyManagement());
        security.put("sponsoredContentFrequency", application.getSponsoredContentFrequency());
        detail.put("securityAssessment", security);

        // Premium calculation
        Map<String, Object> calculation = new HashMap<>();
        calculation.put("basePremium", application.getProduct().getBasePremium());
        calculation.put("finalPremium", application.getCalculatedPremium());
        calculation.put("requiresReview", application.getRequiresReview());
        detail.put("calculation", calculation);

        return detail;
    }
}
