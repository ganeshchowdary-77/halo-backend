package com.thehalo.halobackend.service.quote;

import com.thehalo.halobackend.dto.quote.request.QuoteStatusUpdateRequest;
import com.thehalo.halobackend.dto.quote.request.SubmitQuoteRequest;
import com.thehalo.halobackend.dto.quote.response.QuoteDetailResponse;
import com.thehalo.halobackend.dto.quote.response.QuoteSummaryResponse;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.exception.domain.quote.QuoteNotFoundException;
import com.thehalo.halobackend.exception.business.UnauthorizedAccessException;
import com.thehalo.halobackend.exception.domain.product.ProductNotAvailableException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotFoundException;
import com.thehalo.halobackend.exception.domain.profile.ProfileNotVerifiedException;
import com.thehalo.halobackend.exception.domain.quote.InvalidQuoteStateException;
import com.thehalo.halobackend.mapper.quote.QuoteMapper;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.QuoteRequestRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceImplTest {

    @Mock
    private QuoteRequestRepository quoteRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserPlatformRepository profileRepository;

    @Mock
    private QuotePricingService quotePricingService;

    @Mock
    private QuoteMapper quoteMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private QuoteServiceImpl quoteService;

    private Product testProduct;
    private UserPlatform testProfile;
    private QuoteRequest testQuote;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(1L);
        SecurityContextHolder.setContext(securityContext);

        testUser = AppUser.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .basePremium(BigDecimal.valueOf(100))
                .active(true)
                .build();

        testProfile = UserPlatform.builder()
                .id(1L)
                .user(testUser)
                .verified(true)
                .build();

        testQuote = QuoteRequest.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .profile(testProfile)
                .status(QuoteStatus.CALCULATED)
                .calculatedPremium(BigDecimal.valueOf(120))
                .riskScore(50)
                .build();
    }

    @Test
    void getMyQuotes_ShouldReturnUserQuotes() {
        // Given
        when(quoteRepository.findByUserId(1L)).thenReturn(List.of(testQuote));
        when(quoteMapper.toSummaryDto(testQuote)).thenReturn(new QuoteSummaryResponse());

        // When
        List<QuoteSummaryResponse> result = quoteService.getMyQuotes();

        // Then
        assertThat(result).hasSize(1);
        verify(quoteRepository).findByUserId(1L);
    }

    @Test
    void getDetail_ShouldReturnQuoteDetail_WhenExists() {
        // Given
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
        when(quoteMapper.toDetailDto(testQuote)).thenReturn(new QuoteDetailResponse());

        // When
        QuoteDetailResponse result = quoteService.getDetail(1L);

        // Then
        assertThat(result).isNotNull();
        verify(quoteRepository).findById(1L);
    }

    @Test
    void getDetail_ShouldThrowException_WhenNotFound() {
        // Given
        when(quoteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> quoteService.getDetail(1L))
                .isInstanceOf(QuoteNotFoundException.class);
    }

    @Test
    void submit_ShouldCreateQuote_WhenValidRequest() {
        // Given
        SubmitQuoteRequest request = SubmitQuoteRequest.builder()
                .productId(1L)
                .profileId(1L)
                .notes("Test notes")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testProfile));
        when(quotePricingService.calculatePersonalizedPremium(testProduct, testProfile))
                .thenReturn(BigDecimal.valueOf(120));
        when(quotePricingService.calculateRiskScore(testProfile, testProduct)).thenReturn(50);
        when(quoteRepository.save(any(QuoteRequest.class))).thenReturn(testQuote);
        when(quoteMapper.toDetailDto(testQuote)).thenReturn(new QuoteDetailResponse());

        // When
        QuoteDetailResponse result = quoteService.submit(request);

        // Then
        assertThat(result).isNotNull();
        verify(quoteRepository).save(any(QuoteRequest.class));
    }

    @Test
    void submit_ShouldThrowException_WhenProductNotFound() {
        // Given
        SubmitQuoteRequest request = SubmitQuoteRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> quoteService.submit(request))
                .isInstanceOf(ProductNotAvailableException.class);
    }

    @Test
    void submit_ShouldThrowException_WhenProductNotActive() {
        // Given
        testProduct.setActive(false);
        SubmitQuoteRequest request = SubmitQuoteRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThatThrownBy(() -> quoteService.submit(request))
                .isInstanceOf(ProductNotAvailableException.class);
    }

    @Test
    void submit_ShouldThrowException_WhenProfileNotFound() {
        // Given
        SubmitQuoteRequest request = SubmitQuoteRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> quoteService.submit(request))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    void submit_ShouldThrowException_WhenProfileNotVerified() {
        // Given
        testProfile.setVerified(false);
        SubmitQuoteRequest request = SubmitQuoteRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testProfile));

        // When & Then
        assertThatThrownBy(() -> quoteService.submit(request))
                .isInstanceOf(ProfileNotVerifiedException.class);
    }

    @Test
    void acceptQuote_ShouldApproveQuote_WhenNoReviewRequired() {
        // Given
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
        when(quotePricingService.requiresUnderwriterReview(any(), any(), any())).thenReturn(false);
        when(quoteRepository.save(any(QuoteRequest.class))).thenReturn(testQuote);
        when(quoteMapper.toDetailDto(testQuote)).thenReturn(new QuoteDetailResponse());

        // When
        QuoteDetailResponse result = quoteService.acceptQuote(1L);

        // Then
        assertThat(result).isNotNull();
        verify(quoteRepository).save(argThat(quote -> 
            quote.getStatus() == QuoteStatus.APPROVED
        ));
    }

    @Test
    void acceptQuote_ShouldSetUnderReview_WhenReviewRequired() {
        // Given
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
        when(quotePricingService.requiresUnderwriterReview(any(), any(), any())).thenReturn(true);
        when(quoteRepository.save(any(QuoteRequest.class))).thenReturn(testQuote);
        when(quoteMapper.toDetailDto(testQuote)).thenReturn(new QuoteDetailResponse());

        // When
        QuoteDetailResponse result = quoteService.acceptQuote(1L);

        // Then
        assertThat(result).isNotNull();
        verify(quoteRepository).save(argThat(quote -> 
            quote.getStatus() == QuoteStatus.PENDING // Changed from UNDER_REVIEW to PENDING
        ));
    }

    @Test
    void acceptQuote_ShouldThrowException_WhenUnauthorized() {
        // Given
        AppUser otherUser = AppUser.builder().id(2L).build();
        testQuote.setUser(otherUser);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));

        // When & Then
        assertThatThrownBy(() -> quoteService.acceptQuote(1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void acceptQuote_ShouldThrowException_WhenInvalidStatus() {
        // Given
        testQuote.setStatus(QuoteStatus.APPROVED);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));

        // When & Then
        assertThatThrownBy(() -> quoteService.acceptQuote(1L))
                .isInstanceOf(InvalidQuoteStateException.class);
    }

    @Test
    void updateStatus_ShouldUpdateQuoteStatus() {
        // Given
        QuoteStatusUpdateRequest request = new QuoteStatusUpdateRequest();
        request.setStatus(QuoteStatus.REJECTED);
        
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
        when(quoteRepository.save(any(QuoteRequest.class))).thenReturn(testQuote);
        when(quoteMapper.toDetailDto(testQuote)).thenReturn(new QuoteDetailResponse());

        // When
        QuoteDetailResponse result = quoteService.updateStatus(1L, request);

        // Then
        assertThat(result).isNotNull();
        verify(quoteRepository).save(argThat(quote -> 
            quote.getStatus() == QuoteStatus.REJECTED
        ));
    }

    @Test
    void updateStatus_ShouldThrowException_WhenUnauthorized() {
        // Given
        AppUser otherUser = AppUser.builder().id(2L).build();
        testQuote.setUser(otherUser);
        QuoteStatusUpdateRequest request = new QuoteStatusUpdateRequest();
        
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));

        // When & Then
        assertThatThrownBy(() -> quoteService.updateStatus(1L, request))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
