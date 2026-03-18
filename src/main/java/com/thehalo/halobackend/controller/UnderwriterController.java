package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.dto.common.HaloApiResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.dto.underwriter.response.PremiumCalculationLogResponse;
import com.thehalo.halobackend.dto.underwriter.response.QuoteReviewResponse;
import com.thehalo.halobackend.dto.underwriter.response.RiskParameterResponse;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.model.underwriting.RiskParameter;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import com.thehalo.halobackend.service.quote.QuotePricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Underwriter Controller
 * Handles the quote review queue and calculation transparency.
 */
@RestController
@RequestMapping("/api/v1/underwriter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('UNDERWRITER')")
@Tag(name = "Underwriter Queue", description = "Endpoints for underwriters to review pending quotes")
public class UnderwriterController {

    private final RiskParameterRepository riskParameterRepository;
    private final QuoteRequestRepository quoteRequestRepository;
    private final QuotePricingService quotePricingService;

    // ==================== QUOTE REVIEW QUEUE ====================

    /**
     * GET /api/v1/underwriter/quotes/pending
     * View all quotes requiring underwriter review (paginated)
     * Returns only UNASSIGNED quotes in PENDING status
     */
    @GetMapping("/quotes/pending")
    @Operation(summary = "Get pending quotes", description = "Retrieves unassigned quotes in PENDING status for the queue")
    public ResponseEntity<HaloApiResponse<Page<QuoteReviewResponse>>> getPendingQuotes(
            @RequestParam(required = false, defaultValue = "") String search,
            org.springframework.data.domain.Pageable pageable) {
        // Get only unassigned PENDING quotes for the queue
        Page<QuoteRequest> pendingQuotes;
        if (search != null && !search.isEmpty()) {
            // If search is provided, filter by search and unassigned
            pendingQuotes = quoteRequestRepository.findBySearchAndStatus(search, QuoteStatus.PENDING, pageable)
                .map(quote -> quote.getAssignedUnderwriter() == null ? quote : null)
                .map(quote -> quote);
        } else {
            // Get unassigned quotes directly
            pendingQuotes = quoteRequestRepository.findUnassignedQuotes(pageable);
        }
        
        Page<QuoteReviewResponse> response = pendingQuotes.map(this::toQuoteReviewResponse);
        
        return ResponseFactory.success(response, "Pending quotes retrieved");
    }

    /**
     * POST /api/v1/underwriter/quotes/{quoteId}/assign
     * Assign quote to current underwriter
     */
    @PostMapping("/quotes/{quoteId}/assign")
    @Operation(summary = "Assign quote", description = "Assigns a quote to the current underwriter for review")
    public ResponseEntity<HaloApiResponse<QuoteReviewResponse>> assignQuote(@PathVariable Long quoteId) {
        QuoteRequest quote = quoteRequestRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new RuntimeException("Only PENDING quotes can be assigned");
        }
        
        quote.setAssignedUnderwriter(getCurrentUser());
        quote.setStatus(QuoteStatus.PENDING); // Keep as PENDING when assigned
        quoteRequestRepository.save(quote);
        
        return ResponseFactory.success(toQuoteReviewResponse(quote), "Quote assigned successfully");
    }

    /**
     * POST /api/v1/underwriter/quotes/{quoteId}/claim
     * Claim a quote for review
     */
    @PostMapping("/quotes/{quoteId}/claim")
    @Operation(summary = "Claim quote", description = "Claims a quote for the current underwriter to review")
    public ResponseEntity<HaloApiResponse<String>> claimQuote(@PathVariable Long quoteId) {
        QuoteRequest quote = quoteRequestRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new RuntimeException("Only PENDING quotes can be claimed");
        }
        
        quote.setAssignedUnderwriter(getCurrentUser());
        quote.setStatus(QuoteStatus.PENDING); // Keep as PENDING when claimed
        quoteRequestRepository.save(quote);
        
        return ResponseFactory.success("Quote claimed successfully", "Quote claimed successfully");
    }

    /**
     * POST /api/v1/underwriter/quotes/{quoteId}/release
     * Release a quote back to the queue
     */
    @PostMapping("/quotes/{quoteId}/release")
    @Operation(summary = "Release quote", description = "Releases a quote back to the available queue")
    public ResponseEntity<HaloApiResponse<String>> releaseQuote(@PathVariable Long quoteId) {
        QuoteRequest quote = quoteRequestRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
                
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new RuntimeException("Only PENDING quotes can be released");
        }
        
        quote.setAssignedUnderwriter(null);
        quote.setStatus(QuoteStatus.PENDING);
        quoteRequestRepository.save(quote);
        
        return ResponseFactory.success("Quote released successfully", "Quote released successfully");
    }

    /**
     * GET /api/v1/underwriter/quotes/assigned
     * Get quotes assigned to current underwriter
     */
    @GetMapping("/quotes/assigned")
    @Operation(summary = "Get assigned quotes", description = "Retrieves quotes assigned to the current underwriter")
    public ResponseEntity<HaloApiResponse<Page<QuoteReviewResponse>>> getAssignedQuotes(
            org.springframework.data.domain.Pageable pageable) {
        
        // Find assigned quotes where underwriter ID matches current user and status is PENDING
        // Wait, standard repository might not have this method.
        // Let's use custom logic or add a method. Let's just use findByStatus for now and filter,
        // or add a method if needed. Let's try adding method or filtering.
        // I will add findByAssignedUnderwriterIdAndStatus in repository later, for now we can filter, or better, let's assume I will add it to repo.
        // Actually, we can use the repository to find by assigned underwriter.
        // Let's check QuoteRequestRepository in a moment.
        // For now, let's use the repository method if it exists, or filter manually if not paginated perfectly.
        // Pageable might fail if I filter manually. Let me just add the method to repository.
        Page<QuoteRequest> assignedQuotes = quoteRequestRepository.findByAssignedUnderwriterIdAndStatus(currentUserId(), QuoteStatus.PENDING, pageable);
        
        Page<QuoteReviewResponse> response = assignedQuotes.map(this::toQuoteReviewResponse);
        
        return ResponseFactory.success(response, "Assigned quotes retrieved");
    }

    /**
     * GET /api/v1/underwriter/queue/priority
     * Get high priority quotes
     */
    @GetMapping("/queue/priority")
    @Operation(summary = "Get priority queue", description = "Retrieves high priority quotes requiring immediate attention")
    public ResponseEntity<HaloApiResponse<List<QuoteReviewResponse>>> getPriorityQueue() {
        // Get quotes that are high priority (high coverage, risky niches, etc.)
        List<QuoteRequest> priorityQuotes = quoteRequestRepository.findByStatus(QuoteStatus.PENDING, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .filter(this::isHighPriority)
                .limit(20)
                .toList();
        
        List<QuoteReviewResponse> response = priorityQuotes.stream()
                .map(this::toQuoteReviewResponse)
                .toList();
        
        return ResponseFactory.success(response, "Priority queue retrieved");
    }

    /**
     * GET /api/v1/underwriter/queue/stats
     * Get underwriter queue statistics
     */
    @GetMapping("/queue/stats")
    @Operation(summary = "Get queue stats", description = "Retrieves underwriter queue statistics and metrics")
    public ResponseEntity<HaloApiResponse<Object>> getQueueStats() {
        long pendingCount = quoteRequestRepository.countByStatus(QuoteStatus.PENDING);
        long approvedTodayCount = quoteRequestRepository.countByStatusAndCreatedAtAfter(
                QuoteStatus.APPROVED, 
                java.time.LocalDateTime.now().minusDays(1)
        );
        
        var stats = java.util.Map.of(
                "activeQuotes", pendingCount,
                "completedToday", approvedTodayCount,
                "avgProcessingTimeMinutes", 45, // Mock value - TODO: calculate actual
                "capacity", getCapacityLevel(pendingCount)
        );
        
        return ResponseFactory.success(stats, "Queue stats retrieved");
    }

    /**
     * POST /api/v1/underwriter/quotes/{quoteId}/approve
     * Approve a quote with final premium
     */
    @PostMapping("/quotes/{quoteId}/approve")
    @Operation(summary = "Approve quote", description = "Approves a quote and sets the final premium")
    public ResponseEntity<HaloApiResponse<QuoteReviewResponse>> approveQuote(
            @PathVariable Long quoteId,
            @RequestBody ApproveQuoteRequest request) {
        QuoteRequest quote = quoteRequestRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        
        // Set the offered premium (use calculated if not provided)
        BigDecimal finalPremium = request.getFinalPremium();
        if (finalPremium == null && quote.getProduct() != null && quote.getProfile() != null) {
            finalPremium = quotePricingService.calculatePersonalizedPremium(quote.getProduct(), quote.getProfile());
        }
        
        quote.setOfferedPremium(finalPremium);
        quote.setStatus(QuoteStatus.APPROVED);
        quote.setNotes(request.getComments());
        quoteRequestRepository.save(quote);
        
        return ResponseFactory.success(toQuoteReviewResponse(quote), "Quote approved successfully");
    }

    /**
     * POST /api/v1/underwriter/quotes/{quoteId}/reject
     * Reject a quote with reason
     */
    @PostMapping("/quotes/{quoteId}/reject")
    @Operation(summary = "Reject quote", description = "Rejects a quote with reason")
    public ResponseEntity<HaloApiResponse<QuoteReviewResponse>> rejectQuote(
            @PathVariable Long quoteId,
            @RequestBody RejectQuoteRequest request) {
        QuoteRequest quote = quoteRequestRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        
        quote.setStatus(QuoteStatus.REJECTED);
        quote.setNotes(request.getReason());
        quoteRequestRepository.save(quote);
        
        return ResponseFactory.success(toQuoteReviewResponse(quote), "Quote rejected successfully");
    }

    /**
     * GET /api/v1/underwriter/logs
     * Get assignment and approval logs
     */
    @GetMapping("/logs")
    @Operation(summary = "Get assignment logs", description = "Retrieves assignment and approval logs for audit trail")
    public ResponseEntity<HaloApiResponse<List<Object>>> getAssignmentLogs() {
        // TODO: Implement logs query
        List<Object> logs = List.of(); // Placeholder
        
        return ResponseFactory.success(logs, "Assignment logs retrieved");
    }

    /**
     * GET /api/v1/underwriter/quotes/{quoteId}/calculation-log
     * View detailed premium calculation breakdown for a quote
     */
    @GetMapping("/quotes/{quoteId}/calculation-log")
    @Operation(summary = "Get calculation log", description = "Returns the step-by-step math used to calculate the premium")
    public ResponseEntity<HaloApiResponse<PremiumCalculationLogResponse>> getCalculationLog(@PathVariable Long quoteId) {
        QuoteRequest quote = quoteRequestRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        
        return ResponseFactory.success(quotePricingService.buildCalculationLog(quote), "Calculation log retrieved");
    }

    /**
     * GET /api/v1/underwriter/quotes/all
     * View all quotes with their calculation details (paginated)
     */
    @GetMapping("/quotes/all")
    @Operation(summary = "Get all quotes", description = "Returns all quotes with calculation summaries, optionally filtered by status")
    public ResponseEntity<HaloApiResponse<Page<QuoteReviewResponse>>> getAllQuotes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "") String search,
            org.springframework.data.domain.Pageable pageable) {
        QuoteStatus statusEnum = null;
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            try {
                statusEnum = QuoteStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }

        Page<QuoteRequest> quotes = quoteRequestRepository.findBySearchAndStatus(search, statusEnum, pageable);
        
        Page<QuoteReviewResponse> response = quotes.map(this::toQuoteReviewResponse);
        
        return ResponseFactory.success(response, "Quotes retrieved");
    }

    // ==================== HELPER METHODS ====================

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }

    private AppUser getCurrentUser() {
        AppUser underwriter = new AppUser();
        underwriter.setId(currentUserId());
        return underwriter;
    }

    private RiskParameterResponse toParameterResponse(RiskParameter parameter) {
        return RiskParameterResponse.builder()
                .id(parameter.getId())
                .paramKey(parameter.getParamKey())
                .multiplier(parameter.getMultiplier())
                .description(parameter.getDescription())
                .active(parameter.getActive())
                .lastModifiedBy(parameter.getLastModifiedBy())
                .lastModifiedDate(parameter.getLastModifiedDate())
                .build();
    }

    private QuoteReviewResponse toQuoteReviewResponse(QuoteRequest quote) {
        // Calculate premium
        BigDecimal calculatedPremium = (quote.getProduct() != null && quote.getProfile() != null) 
                ? quotePricingService.calculatePersonalizedPremium(quote.getProduct(), quote.getProfile())
                : BigDecimal.ZERO;
        
        // Check why it requires review
        String reviewReason = getReviewReason(quote);
        String priority = getPriority(quote);
        
        String influencerName = "Unknown";
        String influencerEmail = "Unknown";
        if (quote.getUser() != null) {
            influencerName = quote.getUser().getFullName();
            influencerEmail = quote.getUser().getEmail();
        }

        return QuoteReviewResponse.builder()
                .quoteId(quote.getId())
                .quoteNumber(quote.getQuoteNumber())
                .status(quote.getStatus() != null ? quote.getStatus().name() : "PENDING")
                .userId(quote.getUser() != null ? quote.getUser().getId() : null)
                .userEmail(influencerEmail)
                .influencerName(influencerName)
                .influencerEmail(influencerEmail)
                .profileId(quote.getProfile() != null ? quote.getProfile().getId() : null)
                .profileHandle(quote.getProfile() != null ? quote.getProfile().getHandle() : "-")
                .platform(quote.getProfile() != null && quote.getProfile().getPlatform() != null 
                        ? quote.getProfile().getPlatform().getName().name() : "-")
                .followerCount(quote.getProfile() != null ? quote.getProfile().getFollowerCount() : 0)
                .engagementRate(quote.getProfile() != null ? quote.getProfile().getEngagementRate() : BigDecimal.ZERO)
                .niche(quote.getProfile() != null && quote.getProfile().getNiche() != null 
                        ? quote.getProfile().getNiche().name() : "-")
                .productId(quote.getProduct() != null ? quote.getProduct().getId() : null)
                .productName(quote.getProduct() != null ? quote.getProduct().getName() : "Unknown Product")
                .requestedCoverage(quote.getProduct() != null ? quote.getProduct().getCoverageAmount() : BigDecimal.ZERO)
                .calculatedPremium(calculatedPremium)
                .estimatedPremium(calculatedPremium)
                .offeredPremium(quote.getOfferedPremium())
                .priority(priority)
                .reviewReason(reviewReason)
                .submittedAt(quote.getCreatedAt())
                .createdAt(quote.getCreatedAt())
                .notes(quote.getNotes())
                .build();
    }

    private String getPriority(QuoteRequest quote) {
        if (quote.getProfile() != null && quote.getProfile().getFollowerCount() > 1000000) {
            return "URGENT";
        }
        if (quote.getProduct() != null && quote.getProduct().getCoverageAmount() != null && 
            quote.getProduct().getCoverageAmount().compareTo(new BigDecimal("1000000")) > 0) {
            return "URGENT";
        }
        if (quote.getProfile() != null && quote.getProfile().getNiche() != null) {
            String nicheName = quote.getProfile().getNiche().name();
            if (nicheName.equals("FINANCE") || nicheName.equals("CRYPTO")) {
                return "HIGH";
            }
        }
        return "NORMAL";
    }

    private String getReviewReason(QuoteRequest quote) {
        List<String> reasons = new java.util.ArrayList<>();
        
        if (quote.getProduct() != null && quote.getProduct().getCoverageAmount() != null && 
            quote.getProduct().getCoverageAmount().compareTo(new BigDecimal("500000")) > 0) {
            reasons.add("High coverage amount (>$500K)");
        }
        
        if (quote.getProfile() != null && quote.getProfile().getNiche() != null) {
            String nicheName = quote.getProfile().getNiche().name();
            if (nicheName.equals("FINANCE") || nicheName.equals("CRYPTO")) {
                reasons.add("High-risk niche (" + nicheName + ")");
            }
        }
        
        if (quote.getProfile() != null && quote.getProfile().getFollowerCount() > 5000000) {
            reasons.add("Very large influencer (>5M followers)");
        }
        
        if (quote.getProfile() != null && quote.getProfile().getEngagementRate() != null && 
            quote.getProfile().getEngagementRate().compareTo(new BigDecimal("0.005")) < 0) {
            reasons.add("Low engagement rate (<0.5%)");
        }
        
        return reasons.isEmpty() ? "Manual review requested" : String.join(", ", reasons);
    }


    
    private String getCapacityLevel(long pendingCount) {
        if (pendingCount > 20) return "FULL";
        if (pendingCount > 10) return "HIGH";
        if (pendingCount > 5) return "MEDIUM";
        return "LOW";
    }

    private boolean isHighPriority(QuoteRequest quote) {
        // High priority criteria
        if (quote.getProduct() != null && quote.getProduct().getCoverageAmount() != null && 
            quote.getProduct().getCoverageAmount().compareTo(new BigDecimal("1000000")) > 0) {
            return true; // High coverage amount
        }
        
        if (quote.getProfile() != null) {
            // High-risk niches
            if (quote.getProfile().getNiche() != null && 
                (quote.getProfile().getNiche() == com.thehalo.halobackend.enums.Niche.FINANCE || 
                 quote.getProfile().getNiche() == com.thehalo.halobackend.enums.Niche.CRYPTO ||
                 quote.getProfile().getNiche() == com.thehalo.halobackend.enums.Niche.POLITICS)) {
                return true;
            }
            
            // Very large influencers
            if (quote.getProfile().getFollowerCount() > 5000000) {
                return true;
            }
            
            // Very low engagement (potential fraud)
            if (quote.getProfile().getEngagementRate() != null && 
                quote.getProfile().getEngagementRate().compareTo(BigDecimal.valueOf(0.005)) < 0) {
                return true;
            }
        }
        
        // Time-based priority (older than 24 hours)
        if (quote.getCreatedAt() != null && 
            quote.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusHours(24))) {
            return true;
        }
        
        return false;
    }



    // Request DTOs
    @lombok.Data
    public static class UpdateRiskParameterRequest {
        private BigDecimal multiplier;
        private String description;
        private Boolean active;
        private String modifiedBy;
    }

    @lombok.Data
    public static class CreateRiskParameterRequest {
        private String paramKey;
        private BigDecimal multiplier;
        private String description;
        private String createdBy;
    }

    @lombok.Data
    public static class ApproveQuoteRequest {
        private BigDecimal finalPremium;
        private String comments;
    }

    @lombok.Data
    public static class RejectQuoteRequest {
        private String reason;
    }
}
