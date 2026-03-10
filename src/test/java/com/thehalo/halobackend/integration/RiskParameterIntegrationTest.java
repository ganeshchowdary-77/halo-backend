package com.thehalo.halobackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.risk.request.UpdateRiskParameterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Risk Parameter Management API endpoints
 * Tests the complete workflow: GET, PUT operations with security
 */
@WebMvcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.sql.init.mode=never"
})
public class RiskParameterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    public void testGetAllRiskParameters_WithUnderwriterRole_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/risk-parameters")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    public void testGetAllRiskParameters_WithInfluencerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/risk-parameters"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetAllRiskParameters_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/risk-parameters"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    public void testUpdateRiskParameter_WithValidRequest_ShouldReturnSuccess() throws Exception {
        UpdateRiskParameterRequest request = new UpdateRiskParameterRequest();
        request.setMultiplier(BigDecimal.valueOf(2.0));
        request.setUpdateNote("Updated due to increased market risk");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/risk-parameters/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    public void testUpdateRiskParameter_WithInvalidMultiplier_ShouldReturnBadRequest() throws Exception {
        UpdateRiskParameterRequest request = new UpdateRiskParameterRequest();
        request.setMultiplier(BigDecimal.valueOf(15.0)); // Invalid: > 10.0
        request.setUpdateNote("Test update");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/risk-parameters/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    public void testUpdateRiskParameter_WithShortUpdateNote_ShouldReturnBadRequest() throws Exception {
        UpdateRiskParameterRequest request = new UpdateRiskParameterRequest();
        request.setMultiplier(BigDecimal.valueOf(1.5));
        request.setUpdateNote("Short"); // Invalid: < 10 characters

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/risk-parameters/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "INFLUENCER")
    public void testUpdateRiskParameter_WithInfluencerRole_ShouldReturnForbidden() throws Exception {
        UpdateRiskParameterRequest request = new UpdateRiskParameterRequest();
        request.setMultiplier(BigDecimal.valueOf(1.5));
        request.setUpdateNote("Test update note");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/risk-parameters/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    public void testGetRiskParameterById_WithValidId_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/risk-parameters/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    public void testGetRiskParameterById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/risk-parameters/999"))
                .andExpect(status().isNotFound());
    }
}