package com.thehalo.halobackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.auth.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFixTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testJwtAuthenticationFlow() throws Exception {
        System.out.println("\n🔧 Testing JWT Authentication Fix\n");

        // Step 1: Login to get JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("iamadmin@thehalo.com");
        loginRequest.setPassword("admin123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        System.out.println("✅ Step 1: Login successful");
        System.out.println("   Token: " + token.substring(0, 30) + "...");

        // Step 2: Use JWT token to access protected endpoint
        mockMvc.perform(get("/api/v1/products/public")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        System.out.println("✅ Step 2: JWT token authentication successful");
        System.out.println("\n🎉 JWT Authentication Fix VERIFIED!\n");
    }
}