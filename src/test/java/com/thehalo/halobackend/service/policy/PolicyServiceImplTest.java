package com.thehalo.halobackend.service.policy;

import com.thehalo.halobackend.dto.policy.request.PurchasePolicyRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.enums.PlatformName;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.enums.QuoteStatus;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotFoundException;
import com.thehalo.halobackend.exception.business.UnauthorizedAccessException;
import com.thehalo.halobackend.exception.domain.policy.*;
import com.thehalo.halobackend.mapper.policy.PolicyMapper;
import com.thehalo.halobackend.mapper.quote.QuoteMapper;
import com.thehalo.halobackend.model.platform.Platform;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.policy.QuoteRequest;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.PolicyRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserPlatformRepository profileRepository;

    @Mock
    private QuoteRequestRepository quoteRepository;

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private QuoteMapper quoteMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private AppUser testUser;
    private Product testProduct;
    private UserPlatform testProfile;
    private Platform testPlatform;
    private Policy testPolicy;
    private QuoteRequest testQuote;

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

        testPlatform = new Platform(1L, PlatformName.INSTAGRAM, 1.0, "Instagram");

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .basePremium(BigDecimal.valueOf(100))
                .coverageLimitLegal(BigDecimal.valueOf(50000))
                .coverageLimitPR(BigDecimal.valueOf(30000))
                .coverageLimitMonitoring(BigDecimal.valueOf(20000))
                .active(true)
                .build();

        testProfile = UserPlatform.builder()
                .id(1L)
                .user(testUser)
                .platform(testPlatform)
                .verified(true)
                .build();

        testPolicy = Policy.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .profile(testProfile)
                .policyNumber("POL-001")
                .status(PolicyStatus.ACTIVE)
                .premiumAmount(BigDecimal.valueOf(120))
                .totalCoverageLimit(BigDecimal.valueOf(100000))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

        testQuote = QuoteRequest.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .profile(testProfile)
                .status(QuoteStatus.APPROVED)
                .offeredPremium(BigDecimal.valueOf(120))
                .build();
    }

    @Test
    void getMyPolicies_ShouldReturnUserPolicies() {
        // Given
        when(policyRepository.findByUserId(1L)).thenReturn(List.of(testPolicy));
        when(policyMapper.toSummaryDto(testPolicy)).thenReturn(new PolicySummaryResponse());

        // When
        List<PolicySummaryResponse> result = policyService.getMyPolicies();

        // Then
        assertThat(result).hasSize(1);
        verify(policyRepository).findByUserId(1L);
    }

    @Test
    void getAllPolicies_ShouldReturnAllPolicies() {
        // Given
        when(policyRepository.findAll()).thenReturn(List.of(testPolicy));
        when(policyMapper.toSummaryDto(testPolicy)).thenReturn(new PolicySummaryResponse());

        // When
        List<PolicySummaryResponse> result = policyService.getAllPolicies();

        // Then
        assertThat(result).hasSize(1);
        verify(policyRepository).findAll();
    }

    @Test
    void getDetail_ShouldReturnPolicyDetail_WhenExists() {
        // Given
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(policyMapper.toDetailDto(testPolicy)).thenReturn(new PolicyDetailResponse());

        // When
        PolicyDetailResponse result = policyService.getDetail(1L);

        // Then
        assertThat(result).isNotNull();
        verify(policyRepository).findById(1L);
    }

    @Test
    void getDetail_ShouldThrowException_WhenNotFound() {
        // Given
        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> policyService.getDetail(1L))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void purchase_ShouldCreatePolicy_WhenValidRequest() {
        // Given
        PurchasePolicyRequest request = PurchasePolicyRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testProfile));
        when(policyRepository.findByProfileIdAndProductIdAndStatus(1L, 1L, PolicyStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);
        when(policyMapper.toDetailDto(testPolicy)).thenReturn(new PolicyDetailResponse());

        // When
        PolicyDetailResponse result = policyService.purchase(request);

        // Then
        assertThat(result).isNotNull();
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    void purchase_ShouldThrowException_WhenProductNotFound() {
        // Given
        PurchasePolicyRequest request = PurchasePolicyRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> policyService.purchase(request))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void purchase_ShouldThrowException_WhenProductNotActive() {
        // Given
        testProduct.setActive(false);
        PurchasePolicyRequest request = PurchasePolicyRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThatThrownBy(() -> policyService.purchase(request))
                .isInstanceOf(ProductNotAvailableException.class);
    }

    @Test
    void purchase_ShouldThrowException_WhenProfileNotFound() {
        // Given
        PurchasePolicyRequest request = PurchasePolicyRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> policyService.purchase(request))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void purchase_ShouldThrowException_WhenDuplicateActivePolicy() {
        // Given
        PurchasePolicyRequest request = PurchasePolicyRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testProfile));
        when(policyRepository.findByProfileIdAndProductIdAndStatus(1L, 1L, PolicyStatus.ACTIVE))
                .thenReturn(Optional.of(testPolicy));

        // When & Then
        assertThatThrownBy(() -> policyService.purchase(request))
                .isInstanceOf(DuplicateActivePolicyException.class);
    }

    @Test
    void purchaseFromQuote_ShouldCreatePolicy_WhenValidQuote() {
        // Given
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));
        when(policyRepository.findByProfileIdAndProductIdAndStatus(1L, 1L, PolicyStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(quoteRepository.save(any(QuoteRequest.class))).thenReturn(testQuote);
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);
        when(policyMapper.toDetailDto(testPolicy)).thenReturn(new PolicyDetailResponse());

        // When
        PolicyDetailResponse result = policyService.purchaseFromQuote(1L);

        // Then
        assertThat(result).isNotNull();
        verify(policyRepository).save(any(Policy.class));
        verify(quoteRepository).save(argThat(quote -> 
            quote.getStatus() == QuoteStatus.ACCEPTED
        ));
    }

    @Test
    void purchaseFromQuote_ShouldThrowException_WhenQuoteNotFound() {
        // Given
        when(quoteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> policyService.purchaseFromQuote(1L))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void purchaseFromQuote_ShouldThrowException_WhenUnauthorized() {
        // Given
        AppUser otherUser = AppUser.builder().id(2L).build();
        testQuote.setUser(otherUser);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));

        // When & Then
        assertThatThrownBy(() -> policyService.purchaseFromQuote(1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void purchaseFromQuote_ShouldThrowException_WhenQuoteNotApproved() {
        // Given
        testQuote.setStatus(QuoteStatus.PENDING);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(testQuote));

        // When & Then
        assertThatThrownBy(() -> policyService.purchaseFromQuote(1L))
                .isInstanceOf(InvalidPolicyStateException.class);
    }

    @Test
    void cancel_ShouldCancelPolicy_WhenActive() {
        // Given
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);
        when(policyMapper.toSummaryDto(testPolicy)).thenReturn(new PolicySummaryResponse());

        // When
        PolicySummaryResponse result = policyService.cancel(1L);

        // Then
        assertThat(result).isNotNull();
        verify(policyRepository).save(argThat(policy -> 
            policy.getStatus() == PolicyStatus.CANCELLED
        ));
    }

    @Test
    void cancel_ShouldThrowException_WhenPolicyNotFound() {
        // Given
        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> policyService.cancel(1L))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void cancel_ShouldThrowException_WhenPolicyNotActive() {
        // Given
        testPolicy.setStatus(PolicyStatus.EXPIRED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        // When & Then
        assertThatThrownBy(() -> policyService.cancel(1L))
                .isInstanceOf(PolicyNotActiveException.class);
    }

    @Test
    void payPremium_ShouldActivatePolicy_WhenPendingPayment() {
        // Given
        testPolicy.setStatus(PolicyStatus.PENDING_PAYMENT);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);
        when(policyMapper.toDetailDto(testPolicy)).thenReturn(new PolicyDetailResponse());

        // When
        PolicyDetailResponse result = policyService.payPremium(1L);

        // Then
        assertThat(result).isNotNull();
        verify(policyRepository).save(argThat(policy -> 
            policy.getStatus() == PolicyStatus.ACTIVE
        ));
    }

    @Test
    void payPremium_ShouldThrowException_WhenUnauthorized() {
        // Given
        AppUser otherUser = AppUser.builder().id(2L).build();
        testPolicy.setUser(otherUser);
        testPolicy.setStatus(PolicyStatus.PENDING_PAYMENT);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        // When & Then
        assertThatThrownBy(() -> policyService.payPremium(1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void payPremium_ShouldThrowException_WhenNotPendingPayment() {
        // Given
        testPolicy.setStatus(PolicyStatus.ACTIVE);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        // When & Then
        assertThatThrownBy(() -> policyService.payPremium(1L))
                .isInstanceOf(InvalidPolicyStateException.class);
    }
}
