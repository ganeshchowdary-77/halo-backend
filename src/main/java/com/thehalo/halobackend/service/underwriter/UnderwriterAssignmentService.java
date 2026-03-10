package com.thehalo.halobackend.service.underwriter;

import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.exception.business.QuoteNotFoundException;
import com.thehalo.halobackend.exception.business.UserNotFoundException;
import com.thehalo.halobackend.exception.security.InsufficientRoleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnderwriterAssignmentService {

    private final AppUserRepository userRepository;
    private final QuoteRequestRepository quoteRepository;
    private final ReentrantLock assignmentLock = new ReentrantLock();

    /**
     * Strategy 1: Round Robin Assignment (Automatic)
     * Assigns to underwriter with least active assignments
     */
    @Transactional
    public Optional<AppUser> assignUnderwriterRoundRobin(QuoteRequest quote) {
        assignmentLock.lock();
        try {
            List<AppUser> availableUnderwriters = userRepository
                .findActiveUnderwritersOrderByWorkload();
            
            if (availableUnderwriters.isEmpty()) {
                log.warn("No available underwriters for quote assignment: {}", quote.getQuoteNumber());
                return Optional.empty();
            }

            AppUser assignedUnderwriter = availableUnderwriters.get(0);
            log.info("Assigned quote {} to underwriter {} (Round Robin)", 
                quote.getQuoteNumber(), assignedUnderwriter.getEmail());
            
            return Optional.of(assignedUnderwriter);
            
        } finally {
            assignmentLock.unlock();
        }
    }

    /**
     * Strategy 2: Skill-Based Assignment
     * Assigns based on underwriter expertise/platform experience
     */
    public Optional<AppUser> assignUnderwriterBySkill(QuoteRequest quote) {
        String platformName = quote.getProfile().getPlatform().getName().name();
        
        List<AppUser> skilledUnderwriters = userRepository
            .findUnderwritersByPlatformExperience(platformName);
            
        if (skilledUnderwriters.isEmpty()) {
            // Fallback to round robin
            return assignUnderwriterRoundRobin(quote);
        }
        
        // Get least busy skilled underwriter
        AppUser assignedUnderwriter = skilledUnderwriters.stream()
            .min((u1, u2) -> Integer.compare(
                getActiveAssignmentCount(u1), 
                getActiveAssignmentCount(u2)))
            .orElse(null);
            
        log.info("Assigned quote {} to skilled underwriter {} for platform {}", 
            quote.getQuoteNumber(), assignedUnderwriter.getEmail(), platformName);
            
        return Optional.ofNullable(assignedUnderwriter);
    }

    /**
     * Strategy 3: Queue-Based Assignment (Manual Selection)
     * Underwriters can see available quotes and claim them
     */
    public List<QuoteRequest> getAvailableQuotesForUnderwriter(Long underwriterId) {
        return quoteRepository.findByStatusAndAssignedUnderwriterIsNull(QuoteStatus.PENDING);
    }

    /**
     * Strategy 4: Hybrid Assignment with Timeout
     * Try manual selection first, fallback to auto-assignment after timeout
     */
    @Transactional
    public boolean claimQuoteForUnderwriter(Long quoteId, Long underwriterId) {
        assignmentLock.lock();
        try {
            QuoteRequest quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException("Quote not found: " + quoteId));
                
            if (quote.getAssignedUnderwriter() != null) {
                log.warn("Quote {} already assigned to underwriter {}", 
                    quote.getQuoteNumber(), quote.getAssignedUnderwriter().getId());
                return false;
            }
            
            AppUser underwriter = userRepository.findById(underwriterId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + underwriterId));
                
            if (!underwriter.getRole().getName().equals(RoleName.UNDERWRITER)) {
                throw new InsufficientRoleException("User is not an underwriter");
            }
            
            quote.setAssignedUnderwriter(underwriter);
            quote.setStatus(QuoteStatus.IN_REVIEW);
            quote.setReviewedAt(LocalDateTime.now());
            quoteRepository.save(quote);
            
            log.info("Underwriter {} claimed quote {}", underwriterId, quote.getQuoteNumber());
            return true;
            
        } finally {
            assignmentLock.unlock();
        }
    }

    /**
     * Auto-assign quotes that haven't been claimed within timeout period
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void autoAssignExpiredQuotes() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        
        List<QuoteRequest> expiredQuotes = quoteRepository
            .findUnassignedQuotesOlderThan(cutoff);
            
        for (QuoteRequest quote : expiredQuotes) {
            assignUnderwriterRoundRobin(quote)
                .ifPresent(underwriter -> {
                    quote.setAssignedUnderwriter(underwriter);
                    quote.setStatus(QuoteStatus.IN_REVIEW);
                    quote.setReviewedAt(LocalDateTime.now());
                    quoteRepository.save(quote);
                    
                    log.info("Auto-assigned expired quote {} to underwriter {}", 
                        quote.getQuoteNumber(), underwriter.getId());
                });
        }
    }

    private int getActiveAssignmentCount(AppUser underwriter) {
        return quoteRepository.countActiveQuotesByUnderwriter(underwriter.getId());
    }
}