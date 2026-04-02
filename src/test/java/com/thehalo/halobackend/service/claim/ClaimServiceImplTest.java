package com.thehalo.halobackend.service.claim;

import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.exception.business.UnauthorizedAccessException;
import com.thehalo.halobackend.exception.domain.claim.ClaimNotFoundException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotActiveException;
import com.thehalo.halobackend.exception.domain.policy.PolicyNotFoundException;
import com.thehalo.halobackend.mapper.claim.ClaimMapper;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.policy.Policy;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.ClaimRepository;
import com.thehalo.halobackend.repository.PolicyRepository;
import com.thehalo.halobackend.security.service.CustomUserDetails;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private com.thehalo.halobackend.repository.UserPlatformRepository profileRepository;

    @Mock
    private ClaimMapper claimMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private AppUser testUser;
    private Policy testPolicy;
    private Claim testClaim;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUserId()).thenReturn(1L);
        SecurityContextHolder.setContext(securityContext);

        testUser = AppUser.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        testPolicy = Policy.builder()
                .id(1L)
                .user(testUser)
                .status(PolicyStatus.ACTIVE)
                .totalCoverageLimit(BigDecimal.valueOf(100000))
                .build();

        testClaim = Claim.builder()
                .id(1L)
                .policy(testPolicy)
                .claimNumber("CLM-001")
                .description("Test claim")
                .claimAmount(BigDecimal.valueOf(5000))
                .status(ClaimStatus.SUBMITTED)
                .build();
    }

    @Test
    void getMyClaims_ShouldReturnUserClaims() {
        when(claimRepository.findByPolicyUserId(1L)).thenReturn(List.of(testClaim));
        when(claimMapper.toSummaryDto(testClaim)).thenReturn(new ClaimSummaryResponse());

        List<ClaimSummaryResponse> result = claimService.getMyClaims();

        assertThat(result).hasSize(1);
        verify(claimRepository).findByPolicyUserId(1L);
    }

    @Test
    void submit_ShouldCreateClaim_WhenValidRequest() {
        FileClaimRequest request = FileClaimRequest.builder()
                .policyId(1L)
                .description("Test claim")
                .claimAmount(BigDecimal.valueOf(5000))
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(claimRepository.save(any(Claim.class))).thenReturn(testClaim);
        when(claimMapper.toDetailDto(testClaim)).thenReturn(new ClaimDetailResponse());

        ClaimDetailResponse result = claimService.file(request, List.of());

        assertThat(result).isNotNull();
        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    void submit_ShouldThrowException_WhenPolicyNotFound() {
        FileClaimRequest request = FileClaimRequest.builder()
                .policyId(1L)
                .description("Test claim")
                .claimAmount(BigDecimal.valueOf(5000))
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.file(request, List.of()))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void submit_ShouldThrowException_WhenPolicyNotActive() {
        testPolicy.setStatus(PolicyStatus.EXPIRED);
        FileClaimRequest request = FileClaimRequest.builder()
                .policyId(1L)
                .description("Test claim")
                .claimAmount(BigDecimal.valueOf(5000))
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        assertThatThrownBy(() -> claimService.file(request, List.of()))
                .isInstanceOf(PolicyNotActiveException.class);
    }

    @Test
    void submit_ShouldThrowException_WhenUnauthorized() {
        AppUser otherUser = AppUser.builder().id(2L).build();
        testPolicy.setUser(otherUser);
        
        FileClaimRequest request = FileClaimRequest.builder()
                .policyId(1L)
                .description("Test claim")
                .claimAmount(BigDecimal.valueOf(5000))
                .build();

        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        assertThatThrownBy(() -> claimService.file(request, List.of()))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void getDetail_ShouldReturnClaimDetail_WhenExists() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(testClaim));
        when(claimMapper.toDetailDto(testClaim)).thenReturn(new ClaimDetailResponse());

        ClaimDetailResponse result = claimService.getDetail(1L);

        assertThat(result).isNotNull();
        verify(claimRepository).findById(1L);
    }

    @Test
    void getDetail_ShouldThrowException_WhenNotFound() {
        when(claimRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> claimService.getDetail(1L))
                .isInstanceOf(ClaimNotFoundException.class);
    }

    @Test
    void getClaimQueue_ShouldReturnAllClaims() {
        when(claimRepository.findAll()).thenReturn(List.of(testClaim));
        when(claimMapper.toSummaryDto(testClaim)).thenReturn(new ClaimSummaryResponse());

        List<ClaimSummaryResponse> result = claimService.getClaimQueue();

        assertThat(result).hasSize(1);
        verify(claimRepository).findAll();
    }
}
