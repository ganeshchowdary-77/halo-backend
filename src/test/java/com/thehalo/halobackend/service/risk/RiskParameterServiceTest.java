package com.thehalo.halobackend.service.risk;

import com.thehalo.halobackend.dto.risk.request.UpdateRiskParameterRequest;
import com.thehalo.halobackend.dto.risk.response.RiskParameterResponse;
import com.thehalo.halobackend.exception.business.ResourceNotFoundException;
import com.thehalo.halobackend.model.RiskParameter;
import com.thehalo.halobackend.model.profile.AppUser;
import com.thehalo.halobackend.repository.AppUserRepository;
import com.thehalo.halobackend.repository.RiskParameterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskParameterServiceTest {

    @Mock
    private RiskParameterRepository riskParameterRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private RiskParameterServiceImpl riskParameterService;

    private RiskParameter testParameter;
    private AppUser testUser;
    private UpdateRiskParameterRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setEmail("underwriter@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("Underwriter");

        // Create test risk parameter
        testParameter = RiskParameter.builder()
                .id(1L)
                .paramKey("NICHE_POLITICS")
                .label("Political Content")
                .multiplier(BigDecimal.valueOf(1.8))
                .description("80% premium surcharge for political content")
                .active(true)
                .updateNote("Initial setup")
                .updatedByUser(testUser)
                .build();
        testParameter.setUpdatedAt(LocalDateTime.now());

        // Create update request
        updateRequest = new UpdateRiskParameterRequest();
        updateRequest.setMultiplier(BigDecimal.valueOf(2.0));
        updateRequest.setUpdateNote("Increased due to higher risk assessment");
    }

    @Test
    void getAllParameters_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<RiskParameter> mockPage = new PageImpl<>(List.of(testParameter));
        when(riskParameterRepository.findByActiveTrue(pageable)).thenReturn(mockPage);

        // Act
        Page<RiskParameterResponse> result = riskParameterService.getAllParameters(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        RiskParameterResponse response = result.getContent().get(0);
        assertEquals(testParameter.getId(), response.getId());
        assertEquals(testParameter.getParamKey(), response.getParamKey());
        assertEquals(testParameter.getLabel(), response.getLabel());
        assertEquals(testParameter.getMultiplier(), response.getMultiplier());
        assertEquals("Test Underwriter", response.getUpdatedByUserName());
    }

    @Test
    void getParameterById_WhenExists_ShouldReturnParameter() {
        // Arrange
        when(riskParameterRepository.findById(1L)).thenReturn(Optional.of(testParameter));

        // Act
        RiskParameterResponse result = riskParameterService.getParameterById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testParameter.getId(), result.getId());
        assertEquals(testParameter.getParamKey(), result.getParamKey());
        assertEquals(testParameter.getLabel(), result.getLabel());
        assertEquals(testParameter.getMultiplier(), result.getMultiplier());
    }

    @Test
    void getParameterById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(riskParameterRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> riskParameterService.getParameterById(999L));
    }

    @Test
    void updateParameter_WhenValidRequest_ShouldUpdateAndReturn() {
        // Arrange
        when(riskParameterRepository.findById(1L)).thenReturn(Optional.of(testParameter));
        when(appUserRepository.findByEmail("underwriter@test.com")).thenReturn(Optional.of(testUser));
        when(riskParameterRepository.save(any(RiskParameter.class))).thenReturn(testParameter);

        // Act
        RiskParameterResponse result = riskParameterService.updateParameter(
                1L, updateRequest, "underwriter@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(updateRequest.getMultiplier(), testParameter.getMultiplier());
        assertEquals(updateRequest.getUpdateNote(), testParameter.getUpdateNote());
        assertEquals(testUser, testParameter.getUpdatedByUser());
        
        verify(riskParameterRepository).save(testParameter);
    }

    @Test
    void updateParameter_WhenParameterNotFound_ShouldThrowException() {
        // Arrange
        when(riskParameterRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> riskParameterService.updateParameter(999L, updateRequest, "underwriter@test.com"));
    }

    @Test
    void updateParameter_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(riskParameterRepository.findById(1L)).thenReturn(Optional.of(testParameter));
        when(appUserRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> riskParameterService.updateParameter(1L, updateRequest, "nonexistent@test.com"));
    }
}