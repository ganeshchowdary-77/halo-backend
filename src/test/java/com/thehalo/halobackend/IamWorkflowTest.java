package com.thehalo.halobackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehalo.halobackend.dto.auth.request.LoginRequest;
import com.thehalo.halobackend.dto.iam.request.CreateStaffRequest;
import com.thehalo.halobackend.enums.RoleName;
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
class IamWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCompleteIamWorkflow() throws Exception {
        System.out.println("\n🚀 Starting Complete IAM Workflow Test\n");

        // Step 1: Login as IAM Admin
        System.out.println("📝 Step 1: Login as IAM Admin");
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("iamadmin@thehalo.com");
        adminLogin.setPassword("admin123");

        MvcResult adminResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("IAM_ADMIN"))
                .andReturn();

        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        System.out.println("   ✅ IAM Admin logged in successfully");
        System.out.println("   🔑 Token: " + adminToken.substring(0, 30) + "...\n");

        // Step 2: Create Policy Admin
        System.out.println("📝 Step 2: Create Policy Admin");
        CreateStaffRequest policyAdmin = new CreateStaffRequest();
        policyAdmin.setEmail("policy" + System.currentTimeMillis() + "@thehalo.com");
        policyAdmin.setFullName("Policy Administrator");
        policyAdmin.setPassword("secure123");
        policyAdmin.setRole(RoleName.POLICY_ADMIN);

        MvcResult policyResult = mockMvc.perform(post("/api/v1/iam/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyAdmin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("POLICY_ADMIN"))
                .andReturn();

        System.out.println("   ✅ Policy Admin created: " + policyAdmin.getEmail() + "\n");

        // Step 3: Create Claims Officer
        System.out.println("📝 Step 3: Create Claims Officer");
        CreateStaffRequest claimsOfficer = new CreateStaffRequest();
        claimsOfficer.setEmail("claims" + System.currentTimeMillis() + "@thehalo.com");
        claimsOfficer.setFullName("Claims Officer");
        claimsOfficer.setPassword("secure123");
        claimsOfficer.setRole(RoleName.CLAIMS_OFFICER);

        mockMvc.perform(post("/api/v1/iam/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimsOfficer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("CLAIMS_OFFICER"));

        System.out.println("   ✅ Claims Officer created: " + claimsOfficer.getEmail() + "\n");

        // Step 4: Create Underwriter
        System.out.println("📝 Step 4: Create Underwriter");
        CreateStaffRequest underwriter = new CreateStaffRequest();
        underwriter.setEmail("underwriter" + System.currentTimeMillis() + "@thehalo.com");
        underwriter.setFullName("Underwriter");
        underwriter.setPassword("secure123");
        underwriter.setRole(RoleName.UNDERWRITER);

        mockMvc.perform(post("/api/v1/iam/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(underwriter)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("UNDERWRITER"));

        System.out.println("   ✅ Underwriter created: " + underwriter.getEmail() + "\n");

        // Step 5: Verify cannot create INFLUENCER via IAM
        System.out.println("📝 Step 5: Verify INFLUENCER cannot be created via IAM");
        CreateStaffRequest influencer = new CreateStaffRequest();
        influencer.setEmail("influencer@test.com");
        influencer.setFullName("Test Influencer");
        influencer.setPassword("test123");
        influencer.setRole(RoleName.INFLUENCER);

        mockMvc.perform(post("/api/v1/iam/staff")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(influencer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        System.out.println("   ✅ Correctly blocked INFLUENCER creation via IAM\n");

        // Step 6: Login with newly created Policy Admin
        System.out.println("📝 Step 6: Login with newly created Policy Admin");
        LoginRequest policyLogin = new LoginRequest();
        policyLogin.setEmail(policyAdmin.getEmail());
        policyLogin.setPassword("secure123");

        MvcResult policyLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("POLICY_ADMIN"))
                .andReturn();

        String policyToken = objectMapper.readTree(policyLoginResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        System.out.println("   ✅ Policy Admin logged in successfully");
        System.out.println("   🔑 Token: " + policyToken.substring(0, 30) + "...\n");

        System.out.println("🎉 COMPLETE IAM WORKFLOW TEST PASSED!");
        System.out.println("\n📊 Summary:");
        System.out.println("   ✅ IAM Admin login");
        System.out.println("   ✅ Policy Admin created & verified");
        System.out.println("   ✅ Claims Officer created");
        System.out.println("   ✅ Underwriter created");
        System.out.println("   ✅ INFLUENCER creation blocked (security)");
        System.out.println("   ✅ New staff login verified");
        System.out.println("\n🚀 All systems operational!\n");
    }
}
