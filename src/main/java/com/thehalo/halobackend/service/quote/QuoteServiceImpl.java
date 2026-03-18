package com.thehalo.halobackend.service.quote;

import com.thehalo.halobackend.dto.quote.request.SubmitQuoteRequest;
import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.exception.domain.quote.QuoteNotFoundException;
import com.thehalo.halobackend.exception.business.UnauthorizedAccessException;
import com.thehalo.halobackend.exception.domain.quote.InvalidQuoteStateException;
import com.thehalo.halobackend.exception.domain.product.ProductNotAvailableException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotVerifiedException;
import com.thehalo.halobackend.mapper.quote.QuoteMapper;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.utility.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRequestRepository quoteRepository;
    private final ProductRepository productRepository;
    private final UserPlatformRepository profileRepository;
    private final QuotePricingService quotePricingService;
    private final QuoteMapper quoteMapper;

    public List<QuoteSummaryResponse> getMyQuotes() {
        return quoteRepository.findByUserId(currentUserId())
                .stream().map(quoteMapper::toSummaryDto).toList();
    }

    public QuoteDetailResponse getDetail(Long quoteId) {
        QuoteRequest quote = findOrThrow(quoteId);
        return quoteMapper.toDetailDto(quote);
    }

    @Transactional
    public QuoteDetailResponse submit(SubmitQuoteRequest request) {
        Long userId = currentUserId();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotAvailableException(request.getProductId()));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductNotAvailableException(request.getProductId());
        }

        UserPlatform profile = profileRepository.findByIdAndUserId(request.getProfileId(), userId)
                .orElseThrow(() -> new ProfileNotFoundException(request.getProfileId()));

        if (!Boolean.TRUE.equals(profile.getVerified())) {
            throw new ProfileNotVerifiedException(profile.getId());
        }

        AppUser user = new AppUser();
        user.setId(userId);

        // Calculate premium and risk score
        BigDecimal calculatedPremium = quotePricingService.calculatePersonalizedPremium(product, profile);
        int riskScore = quotePricingService.calculateRiskScore(profile, product);
        
        // Check if requires underwriter review
        boolean requiresReview = quotePricingService.requiresUnderwriterReview(product, profile, product.getCoverageAmount());
        
        // Determine initial status based on auto-approval logic
        QuoteStatus initialStatus;
        BigDecimal offeredPremium = null;
        
        if (requiresReview) {
            // Requires underwriter review
            initialStatus = QuoteStatus.PENDING; // Changed from UNDER_REVIEW to PENDING
        } else {
            // Auto-approved
            initialStatus = QuoteStatus.APPROVED;
            offeredPremium = calculatedPremium;
        }
        
        QuoteRequest quote = QuoteRequest.builder()
                .quoteNumber(IdGeneratorUtil.generateQuoteNumber())
                .user(user)
                .profile(profile)
                .product(product)
                .status(initialStatus)
                .calculatedPremium(calculatedPremium)
                .offeredPremium(offeredPremium)
                .riskScore(riskScore)
                .requiresReview(requiresReview)
                .notes(request.getNotes())
                .build();

        QuoteRequest saved = quoteRepository.save(quote);
        return quoteMapper.toDetailDto(saved);
    }

    /**
     * User accepts the calculated quote
     */
    @Transactional
    public QuoteDetailResponse acceptQuote(Long quoteId) {
        QuoteRequest quote = findOrThrow(quoteId);
        
        // Security check
        if (!quote.getUser().getId().equals(currentUserId())) {
            throw new UnauthorizedAccessException("You do not have permission to accept this quote");
        }
        
        if (quote.getStatus() != QuoteStatus.CALCULATED) {
            throw new InvalidQuoteStateException("Quote cannot be accepted in current status: " + quote.getStatus());
        }

        // STEP 2: Check if requires underwriter review based on risk score
        boolean requiresReview = quotePricingService.requiresUnderwriterReview(
            quote.getProduct(), quote.getProfile(), quote.getProduct().getCoverageAmount());
        
        quote.setRequiresReview(requiresReview);
        quote.setAcceptedAt(LocalDateTime.now());
        
        if (requiresReview) {
            quote.setStatus(QuoteStatus.PENDING); // Changed from UNDER_REVIEW to PENDING
            // TODO: Notify underwriters
        } else {
            // Auto-approve
            quote.setStatus(QuoteStatus.APPROVED);
            quote.setOfferedPremium(quote.getCalculatedPremium());
        }
        
        QuoteRequest saved = quoteRepository.save(quote);
        return quoteMapper.toDetailDto(saved);
    }

    @Override
    @Transactional
    public QuoteDetailResponse updateStatus(Long quoteId, com.thehalo.halobackend.dto.quote.request.QuoteStatusUpdateRequest request) {
        QuoteRequest quote = findOrThrow(quoteId);

        // Security check: only the owner can reject/update
        if (!quote.getUser().getId().equals(currentUserId())) {
            throw new UnauthorizedAccessException("You do not have permission to update this quote");
        }

        quote.setStatus(request.getStatus());
        QuoteRequest saved = quoteRepository.save(quote);
        return quoteMapper.toDetailDto(saved);
    }

    private QuoteRequest findOrThrow(Long id) {
        return quoteRepository.findById(id).orElseThrow(() -> new QuoteNotFoundException(id));
    }

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }
}
