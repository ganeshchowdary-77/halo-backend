package com.thehalo.halobackend.ai.tools;

import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;
import com.thehalo.halobackend.dto.policy.request.SubmitPolicyApplicationRequest;
import com.thehalo.halobackend.dto.policy.response.PolicyApplicationDetailResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.dto.product.response.ProductDetailResponse;
import com.thehalo.halobackend.dto.product.response.ProductSummaryResponse;
import com.thehalo.halobackend.service.claim.ClaimService;
import com.thehalo.halobackend.service.policy.PolicyApplicationService;
import com.thehalo.halobackend.service.policy.PolicyService;
import com.thehalo.halobackend.service.product.ProductService;
import com.thehalo.halobackend.service.user.UserPlatformService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LangChain4j tools for the INFLUENCER role chatbot.
 *
 * All data access goes through the service layer — never directly to repositories.
 * Services enforce authentication (via SecurityContext), business logic, and premium
 * calculation. The LLM only sees safe, formatted strings.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InfluencerTools {

    private final UserPlatformService        userPlatformService;
    private final ProductService             productService;
    private final PolicyApplicationService   applicationService;
    private final PolicyService              policyService;
    private final ClaimService               claimService;

    /** ThreadLocal isolates each HTTP request's user context in this shared singleton bean. */
    private final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public void setUserId(Long userId) { currentUserId.set(userId); }
    public void clearUserId()          { currentUserId.remove(); }

    // ── Product Discovery ─────────────────────────────────────────────────────

    @Tool("List all available insurance products with their IDs, base premiums, and descriptions")
    public String getAvailableInsuranceProducts() {
        try {
            List<ProductSummaryResponse> products = productService.getActiveSummaries();
            if (products.isEmpty()) return "No insurance products are currently available.";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (ProductSummaryResponse p : products) {
                sb.append(i++).append(". [Product ID: ").append(p.getId()).append("] ")
                  .append(p.getName())
                  .append(" | Base Premium: $").append(p.getBasePremium()).append("/mo")
                  .append(" | ").append(p.getTagline())
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getAvailableInsuranceProducts failed: {}", e.getMessage());
            return "Unable to retrieve products. Please try again.";
        }
    }

    @Tool("Get full details of a specific insurance product by its ID")
    public String getProductDetails(Long productId) {
        try {
            ProductDetailResponse p = productService.getDetail(productId);
            return "Product: " + p.getName() + "\n"
                 + "  Base Premium  : $" + p.getBasePremium() + "/month\n"
                 + "  Coverage Limit: $" + p.getCoverageAmount() + "\n"
                 + "  Description   : " + p.getTagline();
        } catch (Exception e) {
            log.error("getProductDetails failed for id={}: {}", productId, e.getMessage());
            return "Product not found with ID " + productId + ". Use list products to see valid IDs.";
        }
    }

    // ── User-Scoped Data ──────────────────────────────────────────────────────

    @Tool("Get the current user's registered social media platforms with IDs, handles, niches, follower counts, and risk scores. Use the Platform ID when applying for a policy.")
    public String getMyPlatformsAndRiskScores() {
        try {
            List<PlatformSummaryResponse> platforms = userPlatformService.getMyPlatforms();
            if (platforms.isEmpty())
                return "You haven't registered any social media platforms yet. Please add a platform in your dashboard first.";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (PlatformSummaryResponse p : platforms) {
                sb.append(i++).append(". [Platform ID: ").append(p.getId()).append("] ")
                  .append(p.getPlatformName())
                  .append(" | Handle: @").append(p.getHandle())
                  .append(" | Niche: ").append(p.getNiche())
                  .append(" | Followers: ").append(String.format("%,d", p.getFollowerCount()))
                  .append(" | Verified: ").append(p.getVerified() != null && p.getVerified() ? "Yes" : "Pending")
                  .append(" | Risk Level: ").append(p.getRiskLevel() != null ? p.getRiskLevel() : "Not assessed")
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getMyPlatformsAndRiskScores failed: {}", e.getMessage());
            return "Unable to retrieve your platforms. Please try again.";
        }
    }

    @Tool("Get the current user's policy applications and their statuses")
    public String getMyApplications() {
        try {
            List<PolicyApplicationDetailResponse> apps = applicationService.getMyApplications();
            if (apps.isEmpty()) return "You have no submitted policy applications yet.";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (PolicyApplicationDetailResponse a : apps) {
                sb.append(i++).append(". Application #").append(a.getApplicationNumber())
                  .append(" | Status: ").append(a.getStatus())
                  .append(" | Product: ").append(a.getProductName());
                if (a.getCalculatedPremium() != null)
                    sb.append(" | Premium: $").append(a.getCalculatedPremium()).append("/mo");
                if (a.getRiskLevel() != null)
                    sb.append(" | Risk: ").append(a.getRiskLevel());
                if (a.getPolicyId() != null)
                    sb.append(" | Policy Created ✅");
                sb.append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getMyApplications failed: {}", e.getMessage());
            return "Unable to retrieve your applications. Please try again.";
        }
    }

    @Tool("Get the current user's active insurance policies with statuses and premiums")
    public String getMyPolicies() {
        try {
            List<PolicySummaryResponse> policies = policyService.getMyPolicies();
            if (policies.isEmpty()) return "You currently have no active policies.";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (PolicySummaryResponse p : policies) {
                sb.append(i++).append(". Policy #").append(p.getPolicyNumber())
                  .append(" | Status: ").append(p.getStatus())
                  .append(" | Premium: $").append(p.getPremiumAmount()).append("/mo")
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getMyPolicies failed: {}", e.getMessage());
            return "Unable to retrieve your policies. Please try again.";
        }
    }

    @Tool("Get the current user's filed insurance claims with statuses and dates")
    public String getMyClaims() {
        try {
            List<ClaimSummaryResponse> claims = claimService.getMyClaims();
            if (claims.isEmpty()) return "You have no filed claims on record.";
            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (ClaimSummaryResponse c : claims) {
                sb.append(i++).append(". Claim #").append(c.getClaimNumber())
                  .append(" | Amount: $").append(c.getClaimAmount())
                  .append(" | Status: ").append(c.getStatus())
                  .append(" | Date: ").append(c.getIncidentDate())
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getMyClaims failed: {}", e.getMessage());
            return "Unable to retrieve your claims. Please try again.";
        }
    }

    // ── Policy Application Submission ─────────────────────────────────────────

    @Tool("Submit a new policy application. " +
          "CRITICAL: You MUST collect ALL SIX values from the user BEFORE calling this tool. " +
          "Do NOT call this tool until you have asked all 6 security questions and received answers. " +
          "Required parameters: " +
          "1. productId (Long) - from product list, " +
          "2. platformId (Long) - from platform list, " +
          "3. hasTwoFactorAuth (Boolean) - true if user has 2FA enabled, false otherwise, " +
          "4. passwordRotationFrequency (String) - must be exactly 'NEVER', 'MONTHLY', or 'YEARLY', " +
          "5. thirdPartyManagement (Boolean) - true if user uses third-party tools, false otherwise, " +
          "6. sponsoredContentFrequency (String) - must be exactly 'NONE', 'OCCASIONAL', or 'FREQUENT'. " +
          "Ask each question separately and wait for the answer before proceeding to the next question.")
    public String applyForPolicy(Long productId, Long platformId, Boolean hasTwoFactorAuth,
                                 String passwordRotationFrequency, Boolean thirdPartyManagement,
                                 String sponsoredContentFrequency) {
        try {
            SubmitPolicyApplicationRequest req = new SubmitPolicyApplicationRequest();
            req.setProductId(productId);
            req.setProfileId(platformId);
            req.setHasTwoFactorAuth(hasTwoFactorAuth);
            req.setPasswordRotationFrequency(passwordRotationFrequency);
            req.setThirdPartyManagement(thirdPartyManagement);
            req.setSponsoredContentFrequency(sponsoredContentFrequency);

            PolicyApplicationDetailResponse response = applicationService.submitApplication(req);

            if (response.getPolicyId() != null) {
                return "✅ Application #" + response.getApplicationNumber() + " approved!\n"
                     + "  Calculated Premium: $" + response.getCalculatedPremium() + "/month\n"
                     + "  Risk Score: " + response.getRiskScore() + "/100 (" + response.getRiskLevel() + ")\n"
                     + "  Your policy has been created. Visit your Policies dashboard to pay and activate.";
            }
            return "📋 Application #" + response.getApplicationNumber() + " submitted for underwriter review.\n"
                 + "  Calculated Premium: $" + response.getCalculatedPremium() + "/month\n"
                 + "  Risk Score: " + response.getRiskScore() + "/100 (" + response.getRiskLevel() + ")\n"
                 + "  You will be notified once a decision is made.";

        } catch (Exception e) {
            log.error("applyForPolicy failed: {}", e.getMessage());
            return "Failed to submit your application: " + e.getMessage();
        }
    }
}
