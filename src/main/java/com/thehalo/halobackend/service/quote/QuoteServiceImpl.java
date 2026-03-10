package com.thehalo.halobackend.service.quote;

import com.thehalo.halobackend.dto.quote.request.ReviewQuoteRequest;
import com.thehalo.halobackend.dto.quote.request.SubmitQuoteRequest;
import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.ProductNotAvailableException;
import com.thehalo.halobackend.mapper.quote.QuoteMapper;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.model.profile.UserProfile;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.UserProfileRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.utility.IdGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRequestRepository quoteRepository;
    private final ProductRepository productRepository;
    private final UserProfileRepository profileRepository;
    private final QuoteMapper quoteMapper;

    @Transactional(readOnly = true)
    public List<QuoteSummaryResponse> getMyQuotes() {
        return quoteRepository.findByUserId(currentUserId())
                .stream().map(quoteMapper::toSummaryDto).toList();
    }

    @Transactional(readOnly = true)
    public QuoteDetailResponse getDetail(Long quoteId) {
        QuoteRequest quote = findOrThrow(quoteId);
        return quoteMapper.toDetailDto(quote);
    }

    @Transactional
    public QuoteDetailResponse submit(SubmitQuoteRequest request) {
        Long userId = currentUserId();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductNotAvailableException(request.getProductId());
        }

        UserProfile profile = profileRepository.findByIdAndUserId(request.getProfileId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found or not owned by user"));

        AppUser user = new AppUser();
        user.setId(userId);

        QuoteRequest quote = QuoteRequest.builder()
                .quoteNumber(IdGeneratorUtil.generateQuoteNumber())
                .user(user)
                .profile(profile)
                .product(product)
                .status(QuoteStatus.PENDING)
                .notes(request.getNotes())
                .build();

        return quoteMapper.toDetailDto(quoteRepository.save(quote));
    }

    @Transactional(readOnly = true)
    public List<QuoteSummaryResponse> getUnderwriterQueue() {
        return quoteRepository.findByStatus(QuoteStatus.PENDING)
                .stream().map(quoteMapper::toSummaryDto).toList();
    }

    @Transactional
    public QuoteDetailResponse reviewQuote(Long quoteId, ReviewQuoteRequest request, boolean approve) {
        QuoteRequest quote = findOrThrow(quoteId);

        if (quote.getStatus() != QuoteStatus.PENDING && quote.getStatus() != QuoteStatus.REVIEWING) {
            throw new RuntimeException("Quote is no longer pending review");
        }

        AppUser underwriter = new AppUser();
        underwriter.setId(currentUserId());

        quote.setAssignedUnderwriter(underwriter);
        quote.setUnderwriterNotes(request.getUnderwriterNotes());
        quote.setReviewedAt(LocalDateTime.now());

        if (approve) {
            quote.setStatus(QuoteStatus.APPROVED);
            quote.setOfferedPremium(request.getOfferedPremium());
        } else {
            quote.setStatus(QuoteStatus.REJECTED);
        }

        return quoteMapper.toDetailDto(quoteRepository.save(quote));
    }

    private QuoteRequest findOrThrow(Long id) {
        return quoteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quote not found"));
    }

    private Long currentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getUserId();
    }
}
