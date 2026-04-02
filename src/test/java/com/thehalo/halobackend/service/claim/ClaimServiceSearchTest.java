package com.thehalo.halobackend.service.claim;

import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.enums.ClaimStatus;
import com.thehalo.halobackend.enums.ExpenseType;
import com.thehalo.halobackend.mapper.claim.ClaimMapper;
import com.thehalo.halobackend.model.claim.Claim;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClaimService search functionality.
 * Tests the new methods added for Claims Officer AI agent tools.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimService - Search Functionality Tests")
class ClaimServiceSearchTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimMapper claimMapper;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private AppUser testUser;
    private Claim testClaim1;
    private Claim testClaim2;
    private ClaimSummaryResponse testResponse1;
    private ClaimSummaryResponse testResponse2;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setEmail("john.doe@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        // Setup test claims
        testClaim1 = new Claim();
        testClaim1.setId(1L);
        testClaim1.setClaimNumber("CLM-2024-001");
        testClaim1.setStatus(ClaimStatus.SUBMITTED);
        testClaim1.setExpenseType(ExpenseType.LEGAL);
        testClaim1.setClaimAmount(new BigDecimal("5000.00"));
        testClaim1.setIncidentDate(LocalDate.of(2024, 1, 15));
        testClaim1.setFiledBy(testUser);

        testClaim2 = new Claim();
        testClaim2.setId(2L);
        testClaim2.setClaimNumber("CLM-2024-002");
        testClaim2.setStatus(ClaimStatus.UNDER_REVIEW);
        testClaim2.setExpenseType(ExpenseType.REPUTATION);
        testClaim2.setClaimAmount(new BigDecimal("3000.00"));
        testClaim2.setIncidentDate(LocalDate.of(2024, 2, 20));
        testClaim2.setFiledBy(testUser);

        // Setup test responses
        testResponse1 = ClaimSummaryResponse.builder()
                .id(1L)
                .claimNumber("CLM-2024-001")
                .status(ClaimStatus.SUBMITTED)
                .expenseType(ExpenseType.LEGAL)
                .claimAmount(new BigDecimal("5000.00"))
                .incidentDate(LocalDate.of(2024, 1, 15))
                .filedById(1L)
                .filedByEmail("john.doe@example.com")
                .build();

        testResponse2 = ClaimSummaryResponse.builder()
                .id(2L)
                .claimNumber("CLM-2024-002")
                .status(ClaimStatus.UNDER_REVIEW)
                .expenseType(ExpenseType.REPUTATION)
                .claimAmount(new BigDecimal("3000.00"))
                .incidentDate(LocalDate.of(2024, 2, 20))
                .filedById(1L)
                .filedByEmail("john.doe@example.com")
                .build();
    }
