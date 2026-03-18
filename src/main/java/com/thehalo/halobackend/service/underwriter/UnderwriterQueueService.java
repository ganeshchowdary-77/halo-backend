package com.thehalo.halobackend.service.underwriter;

import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.dto.underwriter.response.QueueItemResponse;
import com.thehalo.halobackend.mapper.underwriter.UnderwriterMapper;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.exception.domain.quote.QuoteNotFoundException;
import com.thehalo.halobackend.enums.Niche;
import com.thehalo.halobackend.enums.QuoteStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnderwriterQueueService {

    private final QuoteRequestRepository quoteRepository;
    private final UnderwriterAssignmentService assignmentService;
    private final UnderwriterMapper underwriterMapper;

    // Get available quotes for manual assignment (queue view)
    public Page<QueueItemResponse> getAvailableQuotes(Pageable pageable) {
        Page<QuoteRequest> quotes = quoteRepository.findUnassignedQuotes(pageable);
        
        return quotes.map(quote -> {
            QueueItemResponse item = underwriterMapper.toQueueItem(quote);
            return item.toBuilder().priority(calculatePriority(quote)).build();
        });
    }

    // Get quotes assigned to specific underwriter
    public Page<QueueItemResponse> getAssignedQuotes(Long underwriterId, Pageable pageable) {
        Page<QuoteRequest> quotes = quoteRepository.findByAssignedUnderwriterId(underwriterId, pageable);
        
        return quotes.map(quote -> {
            QueueItemResponse item = underwriterMapper.toQueueItem(quote);
            return item.toBuilder().priority(calculatePriority(quote)).build();
        });
    }

    /**
     * Get high priority quotes (urgent, high value, etc.)
     */
    public List<QueueItemResponse> getHighPriorityQuotes() {
        LocalDateTime urgentCutoff = LocalDateTime.now().minusHours(2);
        
        List<QuoteRequest> urgentQuotes = quoteRepository.findUrgentUnassignedQuotes(urgentCutoff);
        
        return urgentQuotes.stream()
            .map(quote -> {
                QueueItemResponse item = underwriterMapper.toQueueItem(quote);
                return item.toBuilder().priority(calculatePriority(quote)).build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Attempt to claim a quote for an underwriter
     */
    @Transactional
    public boolean claimQuote(Long quoteId, Long underwriterId) {
        return assignmentService.claimQuoteForUnderwriter(quoteId, underwriterId);
    }

    /**
     * Release a quote back to the queue
     */
    @Transactional
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
        

        return true;
    }

    /**
     * Get underwriter workload statistics
     */
    public Map<String, Object> getUnderwriterStats(Long underwriterId) {
        int activeQuotes = quoteRepository.countActiveQuotesByUnderwriter(underwriterId);
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        int completedToday = quoteRepository.countCompletedQuotesToday(underwriterId, startOfDay, endOfDay);
        int avgProcessingTime = quoteRepository.getAvgProcessingTimeMinutes(underwriterId);
        
        return Map.of(
            "activeQuotes", activeQuotes,
            "completedToday", completedToday,
            "avgProcessingTimeMinutes", avgProcessingTime,
            "capacity", getUnderwriterCapacity(activeQuotes)
        );
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
