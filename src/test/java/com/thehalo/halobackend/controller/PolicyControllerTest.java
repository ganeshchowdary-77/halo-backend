package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.policy.request.PurchasePolicyRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
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

@WebMvcTest(PolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyService policyService;

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getMyPolicies_ShouldReturnPolicies() throws Exception {
        when(policyService.getMyPolicies()).thenReturn(List.of(new PolicySummaryResponse()));

        mockMvc.perform(get("/api/v1/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void getDetail_ShouldReturnPolicyDetail() throws Exception {
        when(policyService.getDetail(anyLong())).thenReturn(new PolicyDetailResponse());

        mockMvc.perform(get("/api/v1/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void purchase_ShouldCreatePolicy() throws Exception {
        PurchasePolicyRequest request = PurchasePolicyRequest.builder()
                .productId(1L)
                .profileId(1L)
                .build();

        when(policyService.purchase(any(PurchasePolicyRequest.class)))
                .thenReturn(new PolicyDetailResponse());

        mockMvc.perform(post("/api/v1/policies/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void purchaseFromQuote_ShouldCreatePolicy() throws Exception {
        when(policyService.purchaseFromQuote(anyLong())).thenReturn(new PolicyDetailResponse());

        mockMvc.perform(post("/api/v1/policies/purchase/quote/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void cancel_ShouldCancelPolicy() throws Exception {
        when(policyService.cancel(anyLong())).thenReturn(new PolicySummaryResponse());

        mockMvc.perform(patch("/api/v1/policies/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    void payPremium_ShouldActivatePolicy() throws Exception {
        when(policyService.payPremium(anyLong())).thenReturn(new PolicyDetailResponse());

        mockMvc.perform(post("/api/v1/policies/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "POLICY_ADMIN")
    void getAllPolicies_ShouldReturnAllPolicies() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(List.of(new PolicySummaryResponse()));

        mockMvc.perform(get("/api/v1/policies/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
