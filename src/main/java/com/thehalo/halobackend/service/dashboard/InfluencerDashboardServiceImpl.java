package com.thehalo.halobackend.service.dashboard;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InfluencerDashboardServiceImpl implements InfluencerDashboardService {

    private final PolicyRepository policyRepository;

    @Override
    public Map<String, Object> getDashboardOverview() {
        Long userId = currentUserId();
        List<Policy> activePolicies = policyRepository.findByUserIdAndStatus(userId, PolicyStatus.ACTIVE);
        
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalActivePolicies", activePolicies.size());
        overview.put("totalCoverageAmount", calculateTotalCoverage(activePolicies));
        overview.put("upcomingPayments", getUpcomingPaymentsCount(activePolicies));
        overview.put("overduePayments", getOverduePaymentsCount(activePolicies));
        overview.put("totalPremiumPaid", calculateTotalPremiumPaid(activePolicies));
        
        return overview;
    }

    @Override
    public Map<String, Object> getUpcomingPaymentDues() {
        Long userId = currentUserId();
        List<Policy> activePolicies = policyRepository.findByUserIdAndStatus(userId, PolicyStatus.ACTIVE);
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        List<Map<String, Object>> upcomingDues = activePolicies.stream()
            .filter(policy -> policy.getNextPaymentDueDate() != null)
            .filter(policy -> !policy.getNextPaymentDueDate().isBefore(today))
            .filter(policy -> !policy.getNextPaymentDueDate().isAfter(thirtyDaysFromNow))
            .map(this::mapPolicyToPaymentDue)
            .sorted(Comparator.comparing(m -> (LocalDate) m.get("dueDate")))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> overdueDues = activePolicies.stream()
            .filter(policy -> policy.getNextPaymentDueDate() != null)
            .filter(policy -> policy.getNextPaymentDueDate().isBefore(today))
            .map(this::mapPolicyToPaymentDue)
            .sorted(Comparator.comparing(m -> (LocalDate) m.get("dueDate")))
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("upcoming", upcomingDues);
        result.put("overdue", overdueDues);
        result.put("upcomingCount", upcomingDues.size());
        result.put("overdueCount", overdueDues.size());
        
        return result;
    }

    @Override
    public Map<String, Object> getActivePoliciesSummary() {
        Long userId = currentUserId();
        List<Policy> activePolicies = policyRepository.findByUserIdAndStatus(userId, PolicyStatus.ACTIVE);
        
        List<Map<String, Object>> policySummaries = activePolicies.stream()
            .map(this::mapPolicyToSummary)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("policies", policySummaries);
        result.put("totalCount", policySummaries.size());
        
        return result;
    }

    private Map<String, Object> mapPolicyToPaymentDue(Policy policy) {
        LocalDate dueDate = policy.getNextPaymentDueDate();
        LocalDate today = LocalDate.now();
        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
        
        boolean isPayable = daysUntilDue <= 10 || daysUntilDue < 0; // Payable 10 days before or overdue
        boolean isOverdue = daysUntilDue < 0;
        
        BigDecimal lateFee = BigDecimal.ZERO;
        if (isOverdue) {
            long daysOverdue = Math.abs(daysUntilDue);
            BigDecimal dailyRate = new BigDecimal("0.0005"); // Default 0.05% daily
            lateFee = policy.getPremiumAmount()
                .multiply(dailyRate)
                .multiply(BigDecimal.valueOf(daysOverdue))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        
        Map<String, Object> due = new HashMap<>();
        due.put("policyId", policy.getId());
        due.put("policyNumber", policy.getPolicyNumber());
        due.put("productName", policy.getProduct().getName());
        due.put("premiumAmount", policy.getPremiumAmount());
        due.put("lateFee", lateFee);
        due.put("totalDue", policy.getPremiumAmount().add(lateFee));
        due.put("dueDate", dueDate);
        due.put("daysUntilDue", daysUntilDue);
        due.put("isPayable", isPayable);
        due.put("isOverdue", isOverdue);
        due.put("billingCycle", "MONTHLY");
        
        return due;
    }

    private Map<String, Object> mapPolicyToSummary(Policy policy) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("policyId", policy.getId());
        summary.put("policyNumber", policy.getPolicyNumber());
        summary.put("productName", policy.getProduct().getName());
        summary.put("status", policy.getStatus().name());
        summary.put("premiumAmount", policy.getPremiumAmount());
        summary.put("totalCoverageLimit", policy.getTotalCoverageLimit());
        summary.put("startDate", policy.getStartDate());
        summary.put("endDate", policy.getEndDate());
        summary.put("nextPaymentDue", policy.getNextPaymentDueDate());
        summary.put("totalPremiumPaid", policy.getTotalPremiumPaid());
        
        return summary;
    }

    private BigDecimal calculateTotalCoverage(List<Policy> policies) {
        return policies.stream()
            .map(Policy::getTotalCoverageLimit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long getUpcomingPaymentsCount(List<Policy> policies) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        return policies.stream()
            .filter(p -> p.getNextPaymentDueDate() != null)
            .filter(p -> !p.getNextPaymentDueDate().isBefore(today))
            .filter(p -> !p.getNextPaymentDueDate().isAfter(thirtyDaysFromNow))
            .count();
    }

    private long getOverduePaymentsCount(List<Policy> policies) {
        LocalDate today = LocalDate.now();
        
        return policies.stream()
            .filter(p -> p.getNextPaymentDueDate() != null)
            .filter(p -> p.getNextPaymentDueDate().isBefore(today))
            .count();
    }

    private BigDecimal calculateTotalPremiumPaid(List<Policy> policies) {
        return policies.stream()
            .map(Policy::getTotalPremiumPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
    }
}
