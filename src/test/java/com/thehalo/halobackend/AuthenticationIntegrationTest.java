package com.thehalo.halobackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.auth.request.LoginRequest;
import com.thehalo.halobackend.dto.auth.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testIamAdminLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("iamadmin@thehalo.com");
        loginRequest.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("iamadmin@thehalo.com"))
                .andExpect(jsonPath("$.data.role").value("IAM_ADMIN"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        System.out.println("✅ IAM Admin Login Test PASSED");
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testInfluencerRegistration() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test" + System.currentTimeMillis() + "@example.com");
        registerRequest.setFullName("Test Influencer");
        registerRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("INFLUENCER"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        System.out.println("✅ Influencer Registration Test PASSED");
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("iamadmin@thehalo.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));

        System.out.println("✅ Invalid Credentials Test PASSED");
    }

    @Test
    void testCompleteWorkflow() throws Exception {
        // 1. Login as IAM Admin
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("iamadmin@thehalo.com");
        adminLogin.setPassword("admin123");

        MvcResult adminResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        System.out.println("✅ Step 1: IAM Admin logged in");
        System.out.println("   Token: " + adminToken.substring(0, 20) + "...");

        // 2. Register as Influencer
        RegisterRequest influencerReg = new RegisterRequest();
        influencerReg.setEmail("workflow" + System.currentTimeMillis() + "@example.com");
        influencerReg.setFullName("Workflow Test User");
        influencerReg.setPassword("test123");

        MvcResult influencerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(influencerReg)))
                .andExpect(status().isCreated())
                .andReturn();

        String influencerToken = objectMapper.readTree(influencerResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        System.out.println("✅ Step 2: Influencer registered");
        System.out.println("   Token: " + influencerToken.substring(0, 20) + "...");

        System.out.println("\n🎉 Complete Workflow Test PASSED");
    }
}
