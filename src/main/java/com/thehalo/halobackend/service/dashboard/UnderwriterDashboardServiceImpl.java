package com.thehalo.halobackend.service.dashboard;

import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.exception.domain.quote.QuoteNotFoundException;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
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

    private final QuoteRequestRepository quoteRepository;
    private final PolicyRepository policyRepository;

    @Override
    public Map<String, Object> getOverview() {
        AppUser currentUser = getCurrentUser();
        
        long totalQuotes = quoteRepository.count();
        long approvedQuotes = quoteRepository.countByStatus(QuoteStatus.APPROVED);
        long acceptedQuotes = quoteRepository.countByStatus(QuoteStatus.ACCEPTED);
        long activePolicies = policyRepository.count(); // Changed from totalPolicies to activePolicies for clarity

        int myCompletedToday = quoteRepository.countCompletedQuotesToday(
            currentUser.getId(), 
            java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0),
            java.time.LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
        );
        
        // Calculate start of current week (Monday)
        java.time.LocalDateTime startOfWeek = java.time.LocalDateTime.now()
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
            
        int myCompletedThisWeek = quoteRepository.countCompletedQuotesThisWeek(currentUser.getId(), startOfWeek);
        int myActiveQuotes = quoteRepository.countActiveQuotesByUnderwriter(currentUser.getId());
        int myAvgProcessingTime = quoteRepository.getAvgProcessingTimeMinutes(currentUser.getId());
        
        // Calculate total premium volume from all approved quotes
        BigDecimal totalPremiumVolume = quoteRepository.findByStatus(QuoteStatus.APPROVED)
            .stream()
            .map(QuoteRequest::getOfferedPremium)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Calculate average premium from approved quotes
        BigDecimal avgPremium = approvedQuotes > 0 ? 
            totalPremiumVolume.divide(BigDecimal.valueOf(approvedQuotes), 2, java.math.RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalQuotes", totalQuotes);
        overview.put("approvedQuotes", approvedQuotes);
        overview.put("acceptedQuotes", acceptedQuotes);
        overview.put("activePolicies", activePolicies); // Updated variable name
        overview.put("totalPremiumVolume", totalPremiumVolume);
        overview.put("avgPremium", avgPremium);
        overview.put("conversionRate", totalQuotes > 0 ? (acceptedQuotes * 100.0 / totalQuotes) : 0);
        
        // Personal Metrics
        overview.put("myCompletedToday", myCompletedToday);
        overview.put("myCompletedThisWeek", myCompletedThisWeek);
        overview.put("myActiveQuotes", myActiveQuotes);
        overview.put("myAvgProcessingTime", myAvgProcessingTime);
        overview.put("myEstimatedEarnings", myCompletedThisWeek * 15.0); // $15 per completed quote this week
        
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
        Page<QuoteRequest> quotesPage = quoteRepository.findAll(PageRequest.of(page, size));
        
        List<Map<String, Object>> calculations = quotesPage.getContent().stream()
            .map(this::mapQuoteToPremiumCalculation)
            .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("calculations", calculations);
        result.put("currentPage", page);
        result.put("totalPages", quotesPage.getTotalPages());
        result.put("totalElements", quotesPage.getTotalElements());
        
        return result;
    }

    @Override
    public Map<String, Object> getPremiumCalculationDetail(Long quoteId) {
        QuoteRequest quote = quoteRepository.findById(quoteId)
            .orElseThrow(() -> new QuoteNotFoundException(quoteId));
        
        return buildDetailedCalculation(quote);
    }

    private Map<String, Object> mapQuoteToPremiumCalculation(QuoteRequest quote) {
        Map<String, Object> calc = new HashMap<>();
        calc.put("quoteId", quote.getId());
        calc.put("quoteNumber", quote.getQuoteNumber());
        calc.put("userEmail", quote.getUser().getEmail());
        calc.put("userName", quote.getUser().getFullName());
        calc.put("platform", quote.getProfile().getPlatform().getName().name());
        calc.put("handle", quote.getProfile().getHandle());
        calc.put("followerCount", quote.getProfile().getFollowerCount());
        calc.put("niche", quote.getProfile().getNiche().name());
        calc.put("productName", quote.getProduct().getName());
        calc.put("calculatedPremium", quote.getOfferedPremium());
        calc.put("status", quote.getStatus().name());
        calc.put("createdAt", quote.getCreatedAt());
        
        // Check if converted to policy
        Optional<Policy> policy = policyRepository.findAll().stream()
            .filter(p -> p.getProfile().getId().equals(quote.getProfile().getId()) 
                      && p.getProduct().getId().equals(quote.getProduct().getId()))
            .findFirst();
        
        calc.put("convertedToPolicy", policy.isPresent());
        if (policy.isPresent()) {
            calc.put("policyNumber", policy.get().getPolicyNumber());
            calc.put("policyStatus", policy.get().getStatus().name());
            calc.put("totalPremiumPaid", policy.get().getTotalPremiumPaid());
        }
        
        return calc;
    }

    private Map<String, Object> buildDetailedCalculation(QuoteRequest quote) {
        Map<String, Object> detail = new HashMap<>();
        
        // Basic info
        detail.put("quoteId", quote.getId());
        detail.put("quoteNumber", quote.getQuoteNumber());
        detail.put("status", quote.getStatus().name());
        detail.put("createdAt", quote.getCreatedAt());
        
        // User info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", quote.getUser().getEmail());
        userInfo.put("fullName", quote.getUser().getFullName());
        detail.put("user", userInfo);
        
        // Profile info
        Map<String, Object> profileInfo = new HashMap<>();
        profileInfo.put("platform", quote.getProfile().getPlatform().getName().name());
        profileInfo.put("handle", quote.getProfile().getHandle());
        profileInfo.put("followerCount", quote.getProfile().getFollowerCount());
        profileInfo.put("engagementRate", quote.getProfile().getEngagementRate());
        profileInfo.put("niche", quote.getProfile().getNiche().name());
        profileInfo.put("verified", quote.getProfile().getVerified());
        detail.put("profile", profileInfo);
        
        // Product info
        Map<String, Object> productInfo = new HashMap<>();
        productInfo.put("name", quote.getProduct().getName());
        productInfo.put("basePremium", quote.getProduct().getBasePremium());
        productInfo.put("coverageAmount", quote.getProduct().getCoverageAmount());
        detail.put("product", productInfo);
        
        // Premium calculation breakdown
        BigDecimal basePremium = quote.getProduct().getBasePremium();
        BigDecimal platformMultiplier = getPlatformMultiplier(quote.getProfile().getPlatform().getName().name());
        BigDecimal followerMultiplier = getFollowerMultiplier(quote.getProfile().getFollowerCount());
        BigDecimal nicheMultiplier = getNicheMultiplier(quote.getProfile().getNiche().name());
        
        Map<String, Object> calculation = new HashMap<>();
        calculation.put("basePremium", basePremium);
        calculation.put("platformMultiplier", platformMultiplier);
        calculation.put("followerMultiplier", followerMultiplier);
        calculation.put("nicheMultiplier", nicheMultiplier);
        calculation.put("finalPremium", quote.getOfferedPremium());
        
        List<String> steps = new ArrayList<>();
        steps.add("1. Base Premium: $" + basePremium);
        steps.add("2. Platform Multiplier (×" + platformMultiplier + "): " + quote.getProfile().getPlatform().getName().name());
        steps.add("3. Follower Multiplier (×" + followerMultiplier + "): " + quote.getProfile().getFollowerCount() + " followers");
        steps.add("4. Niche Multiplier (×" + nicheMultiplier + "): " + quote.getProfile().getNiche().name());
        steps.add("5. Final Premium: $" + quote.getOfferedPremium());
        
        calculation.put("calculationSteps", steps);
        detail.put("calculation", calculation);
        
        return detail;
    }

    private BigDecimal getPlatformMultiplier(String platform) {
        // Simplified - in real implementation, fetch from risk_parameters table
        return switch (platform) {
            case "INSTAGRAM" -> BigDecimal.valueOf(1.0);
            case "YOUTUBE" -> BigDecimal.valueOf(0.95);
            case "TIKTOK" -> BigDecimal.valueOf(1.3);
            case "X" -> BigDecimal.valueOf(1.4);
            case "PODCAST" -> BigDecimal.valueOf(0.8);
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal getFollowerMultiplier(int followers) {
        if (followers < 10000) return BigDecimal.valueOf(1.0);
        if (followers < 100000) return BigDecimal.valueOf(1.05);
        if (followers < 500000) return BigDecimal.valueOf(1.1);
        if (followers < 2000000) return BigDecimal.valueOf(1.2);
        return BigDecimal.valueOf(1.35);
    }

    private BigDecimal getNicheMultiplier(String niche) {
        return switch (niche) {
            case "FINANCE" -> BigDecimal.valueOf(2.5);
            case "CRYPTO" -> BigDecimal.valueOf(2.2);
            case "POLITICS" -> BigDecimal.valueOf(1.8);
            case "COMEDY" -> BigDecimal.valueOf(1.4);
            default -> BigDecimal.ONE;
        };
    }
}
