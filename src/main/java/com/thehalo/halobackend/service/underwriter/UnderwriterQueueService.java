package com.thehalo.halobackend.service.underwriter;

import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.dto.underwriter.response.QueueItemResponse;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.exception.business.QuoteNotFoundException;
import com.thehalo.halobackend.enums.Niche;
import com.thehalo.halobackend.enums.QuoteStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnderwriterQueueService {

    private final QuoteRequestRepository quoteRepository;
    private final UnderwriterAssignmentService assignmentService;

    /**
     * Get available quotes for manual assignment (queue view)
     */
    public Page<QueueItemResponse> getAvailableQuotes(Pageable pageable) {
        Page<QuoteRequest> quotes = quoteRepository.findUnassignedQuotes(pageable);
        
        return quotes.map(this::mapToQueueItem);
    }

    /**
     * Get quotes assigned to specific underwriter
     */
    public Page<QueueItemResponse> getAssignedQuotes(Long underwriterId, Pageable pageable) {
        Page<QuoteRequest> quotes = quoteRepository.findByAssignedUnderwriterId(underwriterId, pageable);
        
        return quotes.map(this::mapToQueueItem);
    }

    /**
     * Get high priority quotes (urgent, high value, etc.)
     */
    public List<QueueItemResponse> getHighPriorityQuotes() {
        LocalDateTime urgentCutoff = LocalDateTime.now().minusHours(2);
        
        List<QuoteRequest> urgentQuotes = quoteRepository.findUrgentUnassignedQuotes(urgentCutoff);
        
        return urgentQuotes.stream()
            .map(this::mapToQueueItem)
            .collect(Collectors.toList());
    }

    /**
     * Attempt to claim a quote for an underwriter
     */
    public boolean claimQuote(Long quoteId, Long underwriterId) {
        return assignmentService.claimQuoteForUnderwriter(quoteId, underwriterId);
    }

    /**
     * Release a quote back to the queue
     */
    public boolean releaseQuote(Long quoteId, Long underwriterId) {
        QuoteRequest quote = quoteRepository.findById(quoteId)
            .orElseThrow(() -> new QuoteNotFoundException("Quote not found: " + quoteId));
            
        if (quote.getAssignedUnderwriter() == null || 
            !quote.getAssignedUnderwriter().getId().equals(underwriterId)) {
            return false;
        }
        
        quote.setAssignedUnderwriter(null);
        quote.setStatus(QuoteStatus.PENDING);
        quote.setReviewedAt(null);
        quoteRepository.save(quote);
        
        log.info("Quote {} released back to queue by underwriter {}", quote.getQuoteNumber(), underwriterId);
        return true;
    }

    /**
     * Get underwriter workload statistics
     */
    public Map<String, Object> getUnderwriterStats(Long underwriterId) {
        int activeQuotes = quoteRepository.countActiveQuotesByUnderwriter(underwriterId);
        int completedToday = quoteRepository.countCompletedQuotesToday(underwriterId);
        int avgProcessingTime = quoteRepository.getAvgProcessingTimeMinutes(underwriterId);
        
        return Map.of(
            "activeQuotes", activeQuotes,
            "completedToday", completedToday,
            "avgProcessingTimeMinutes", avgProcessingTime,
            "capacity", getUnderwriterCapacity(activeQuotes)
        );
    }

    private QueueItemResponse mapToQueueItem(QuoteRequest quote) {
        return QueueItemResponse.builder()
            .quoteId(quote.getId())
            .quoteNumber(quote.getQuoteNumber()) // Include quote number for audit trail
            .influencerName(quote.getUser().getFullName())
            .influencerEmail(quote.getUser().getEmail())
            .platform(quote.getProfile().getPlatform().getName().name())
            .niche(quote.getProfile().getNiche().name())
            .followerCount(quote.getProfile().getFollowerCount())
            .requestedCoverage(quote.getProduct().getCoverageAmount())
            .estimatedPremium(quote.getOfferedPremium() != null ? quote.getOfferedPremium() : BigDecimal.ZERO)
            .priority(calculatePriority(quote))
            .createdAt(quote.getCreatedAt())
            .timeInQueue(java.time.Duration.between(quote.getCreatedAt(), LocalDateTime.now()).toMinutes())
            .assignedUnderwriter(quote.getAssignedUnderwriter() != null ? 
                quote.getAssignedUnderwriter().getFullName() : null)
            .status(quote.getStatus().name())
            .productName(quote.getProduct().getName())
            .build();
    }

    private String calculatePriority(QuoteRequest quote) {
        LocalDateTime now = LocalDateTime.now();
        long hoursInQueue = java.time.Duration.between(quote.getCreatedAt(), now).toHours();
        
        // High value quotes (if premium is set)
        if (quote.getOfferedPremium() != null && 
            quote.getOfferedPremium().compareTo(BigDecimal.valueOf(1000)) > 0) {
            return "HIGH";
        }
        
        // Urgent based on time
        if (hoursInQueue > 4) {
            return "URGENT";
        } else if (hoursInQueue > 2) {
            return "HIGH";
        }
        
        // High-risk niches
        if (quote.getProfile().getNiche() == Niche.FINANCE || 
            quote.getProfile().getNiche() == Niche.CRYPTO) {
            return "HIGH";
        }
        
        // Large influencers
        if (quote.getProfile().getFollowerCount() > 1000000) {
            return "HIGH";
        }
        
        return "NORMAL";
    }

    private String getUnderwriterCapacity(int activeQuotes) {
        if (activeQuotes >= 20) return "FULL";
        if (activeQuotes >= 15) return "HIGH";
        if (activeQuotes >= 10) return "MEDIUM";
        return "LOW";
    }
}