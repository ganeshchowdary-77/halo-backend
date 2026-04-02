package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.claim.request.FileClaimRequest;
import com.thehalo.halobackend.dto.claim.response.ClaimDetailResponse;
import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.service.claim.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thehalo.halobackend.security.util.JwtUtil;
import com.thehalo.halobackend.security.service.CustomUserDetailsService;

@WebMvcTest(ClaimController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClaimControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getMyClaims_ShouldReturnClaims() throws Exception {
        when(claimService.getMyClaims()).thenReturn(List.of(new ClaimSummaryResponse()));

        mockMvc.perform(get("/api/v1/claims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void submitClaim_ShouldCreateClaim() throws Exception {
        FileClaimRequest request = FileClaimRequest.builder()
                .policyId(1L)
                .description("Test claim")
                .claimAmount(BigDecimal.valueOf(1000))
                .build();

        when(claimService.file(any(FileClaimRequest.class), any()))
                .thenReturn(new ClaimDetailResponse());

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getClaimDetail_ShouldReturnDetail() throws Exception {
        when(claimService.getDetail(anyLong())).thenReturn(new ClaimDetailResponse());

        mockMvc.perform(get("/api/v1/claims/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
