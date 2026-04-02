package com.thehalo.halobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.auth.response.AuthResponse;
import com.thehalo.halobackend.dto.iam.request.CreateStaffRequest;
import com.thehalo.halobackend.dto.iam.request.UpdateStaffRequest;
import com.thehalo.halobackend.dto.iam.response.StaffSummaryResponse;
import com.thehalo.halobackend.enums.RoleName;
import com.thehalo.halobackend.repository.RefreshTokenRepository;
import com.thehalo.halobackend.service.iam.IamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thehalo.halobackend.security.util.JwtUtil;
import com.thehalo.halobackend.security.service.CustomUserDetailsService;

@WebMvcTest(IamController.class)
@AutoConfigureMockMvc(addFilters = false)
class IamControllerTest {

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IamService iamService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @WithMockUser(roles = "IAM_ADMIN")
    void createStaff_ShouldReturnCreated() throws Exception {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .email("staff@example.com")
                .password("Password123!")
                .fullName("Staff User")
                .role(RoleName.POLICY_ADMIN)
                .build();

        when(iamService.createStaff(any())).thenReturn(AuthResponse.builder().build());

        mockMvc.perform(post("/api/v1/iam/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "IAM_ADMIN")
    void getAllStaff_ShouldReturnList() throws Exception {
        when(iamService.getAllStaff()).thenReturn(List.of(StaffSummaryResponse.builder().build()));

        mockMvc.perform(get("/api/v1/iam/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "IAM_ADMIN")
    void getStaffById_ShouldReturnStaff() throws Exception {
        when(iamService.getStaffById(anyLong())).thenReturn(StaffSummaryResponse.builder().build());

        mockMvc.perform(get("/api/v1/iam/staff/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "IAM_ADMIN")
    void updateStaff_ShouldReturnUpdated() throws Exception {
        UpdateStaffRequest request = new UpdateStaffRequest();
        request.setFullName("Updated Name");

        when(iamService.updateStaff(anyLong(), any())).thenReturn(StaffSummaryResponse.builder().build());

        mockMvc.perform(put("/api/v1/iam/staff/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "IAM_ADMIN")
    void deactivateStaff_ShouldReturnSuccess() throws Exception {
        doNothing().when(iamService).deactivateStaff(anyLong());

        mockMvc.perform(delete("/api/v1/iam/staff/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "IAM_ADMIN")
    void getStaffByRole_ShouldReturnList() throws Exception {
        when(iamService.getStaffByRole(any())).thenReturn(List.of(StaffSummaryResponse.builder().build()));

        mockMvc.perform(get("/api/v1/iam/staff/by-role/POLICY_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "IAM_ADMIN")
    void getActiveSessions_ShouldReturnList() throws Exception {
        when(refreshTokenRepository.findAllByRevokedFalseAndExpiresAtAfter(any(LocalDateTime.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/iam/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
