package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.policy.request.SubmitPolicyApplicationRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;
import com.thehalo.halobackend.service.policy.PolicyApplicationService;
import com.thehalo.halobackend.service.policy.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thehalo.halobackend.security.util.JwtUtil;
import com.thehalo.halobackend.security.service.CustomUserDetailsService;

@WebMvcTest(PolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private PolicyApplicationService applicationService;

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getMyApplications_ShouldReturnApplications() throws Exception {
        when(applicationService.getMyApplications()).thenReturn(List.of(new PolicyApplicationDetailResponse()));

        mockMvc.perform(get("/api/v1/policies/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void applyForPolicy_ShouldCreateApplication() throws Exception {
        SubmitPolicyApplicationRequest request = new SubmitPolicyApplicationRequest();
        request.setProductId(1L);
        request.setProfileId(1L);

        when(applicationService.submitApplication(any(SubmitPolicyApplicationRequest.class)))
                .thenReturn(new PolicyApplicationDetailResponse());

        mockMvc.perform(post("/api/v1/policies/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getApplicationDetail_ShouldReturnApplicationDetail() throws Exception {
        when(applicationService.getApplicationDetail(anyLong())).thenReturn(new PolicyApplicationDetailResponse());

        mockMvc.perform(get("/api/v1/policies/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
