package com.thehalo.halobackend.service.policy;

import com.thehalo.halobackend.dto.policy.request.SubmitPolicyApplicationRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.domain.policy.ApplicationNotFoundException;
import com.thehalo.halobackend.exception.business.UnauthorizedAccessException;
import com.thehalo.halobackend.exception.domain.policy.InvalidApplicationStateException;
import com.thehalo.halobackend.mapper.policy.PolicyApplicationMapper;
import com.thehalo.halobackend.model.policy.Product;
import com.thehalo.halobackend.model.policy.PolicyApplication;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.ProductRepository;
import com.thehalo.halobackend.repository.PolicyApplicationRepository;
import com.thehalo.halobackend.repository.UserPlatformRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import com.thehalo.halobackend.service.underwriting.RiskPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class PolicyApplicationServiceImplTest {

    @Mock
    private PolicyApplicationRepository applicationRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserPlatformRepository profileRepository;

    @Mock
    private RiskPricingService riskPricingService;

    @Mock
    private PolicyApplicationMapper applicationMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private PolicyApplicationServiceImpl applicationService;

    private Product testProduct;
    private UserPlatform testProfile;
    private PolicyApplication testApplication;
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

        testApplication = PolicyApplication.builder()
                .id(1L)
                .user(testUser)
                .product(testProduct)
                .profile(testProfile)
                .status(PolicyStatus.UNDER_REVIEW)
                .calculatedPremium(BigDecimal.valueOf(120))
                .riskScore(50)
                .build();
    }

    @Test
    void getMyApplications_ShouldReturnUserApplications() {
        // Given
        when(applicationRepository.findByUserId(1L)).thenReturn(List.of(testApplication));
        when(applicationMapper.toDetailDto(testApplication)).thenReturn(new PolicyApplicationDetailResponse());

        // When
        List<PolicyApplicationDetailResponse> result = applicationService.getMyApplications();

        // Then
        assertThat(result).hasSize(1);
        verify(applicationRepository).findByUserId(1L);
    }

    @Test
    void getApplicationDetail_ShouldReturnApplicationDetail_WhenExists() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(applicationMapper.toDetailDto(testApplication)).thenReturn(new PolicyApplicationDetailResponse());

        // When
        PolicyApplicationDetailResponse result = applicationService.getApplicationDetail(1L);

        // Then
        assertThat(result).isNotNull();
        verify(applicationRepository).findById(1L);
    }

    @Test
    void getApplicationDetail_ShouldThrowException_WhenNotFound() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> applicationService.getApplicationDetail(1L))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    void submitApplication_ShouldCreateApplication_WhenValidRequest() {
        // Given
        SubmitPolicyApplicationRequest request = new SubmitPolicyApplicationRequest();
        request.setProductId(1L);
        request.setProfileId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testProfile));
        when(riskPricingService.calculatePersonalizedPremium(testProduct, testProfile))
                .thenReturn(BigDecimal.valueOf(120));
        when(riskPricingService.calculateRiskScore(testProfile, testProduct)).thenReturn(50);
        when(applicationRepository.save(any(PolicyApplication.class))).thenReturn(testApplication);
        when(applicationMapper.toDetailDto(testApplication)).thenReturn(new PolicyApplicationDetailResponse());

        // When
        PolicyApplicationDetailResponse result = applicationService.submitApplication(request);

        // Then
        assertThat(result).isNotNull();
        verify(applicationRepository).save(any(PolicyApplication.class));
    }

    @Test
    void submitApplication_ShouldThrowException_WhenDuplicateApplicationExists() {
        // Given
        SubmitPolicyApplicationRequest request = new SubmitPolicyApplicationRequest();
        request.setProductId(1L);
        request.setProfileId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(profileRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testProfile));
        when(applicationRepository.countActiveApplicationsForProfileAndProduct(1L, 1L, 1L)).thenReturn(1L);

        // When & Then
        assertThatThrownBy(() -> applicationService.submitApplication(request))
                .isInstanceOf(InvalidApplicationStateException.class)
                .hasMessageContaining("already have an active");
    }

    @Test
    void approveApplication_ShouldApproveApplication_WhenNoReviewRequired() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        testApplication.setRequiresReview(false);
        when(applicationRepository.save(any(PolicyApplication.class))).thenReturn(testApplication);
        when(applicationMapper.toDetailDto(testApplication)).thenReturn(new PolicyApplicationDetailResponse());

        // When
        PolicyApplicationDetailResponse result = applicationService.approveApplication(1L, "Notes");

        // Then
        assertThat(result).isNotNull();
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == PolicyStatus.PENDING_PAYMENT
        ));
    }

    @Test
    void approveApplication_ShouldSetUnderReview_WhenReviewRequired() {
        // Given
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        testApplication.setRequiresReview(true);
        when(applicationRepository.save(any(PolicyApplication.class))).thenReturn(testApplication);
        when(applicationMapper.toDetailDto(testApplication)).thenReturn(new PolicyApplicationDetailResponse());

        // When
        PolicyApplicationDetailResponse result = applicationService.approveApplication(1L, "Notes");

        // Then
        assertThat(result).isNotNull();
        verify(applicationRepository).save(argThat(app -> 
            app.getStatus() == PolicyStatus.UNDER_REVIEW
        ));
    }

    @Test
    void approveApplication_ShouldThrowException_WhenUnauthorized() {
        // Given
        AppUser otherUser = AppUser.builder().id(2L).build();
        testApplication.setUser(otherUser);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        // When & Then
        assertThatThrownBy(() -> applicationService.approveApplication(1L, "Notes"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
