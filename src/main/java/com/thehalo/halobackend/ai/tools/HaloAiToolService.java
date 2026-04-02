package com.thehalo.halobackend.ai.tools;

import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;
import com.thehalo.halobackend.dto.policy.response.PolicySummaryResponse;
import com.thehalo.halobackend.service.claim.ClaimService;
import com.thehalo.halobackend.service.policy.PolicyService;
import com.thehalo.halobackend.service.user.UserPlatformService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Shared AI tools — risk score, policies, claims.
 * All data access goes through the service layer (auth enforced via SecurityContext).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HaloAiToolService {

    private final UserPlatformService userPlatformService;
    private final PolicyService       policyService;
    private final ClaimService        claimService;

    private final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public void setUserId(Long userId) { currentUserId.set(userId); }
    public void clearUserId()          { currentUserId.remove(); }

    @Tool("Get the authenticated user's composite risk level across all their registered social media platforms")
    public String getUserRiskScore(Long userId) {
        try {
            List<PlatformSummaryResponse> platforms = userPlatformService.getMyPlatforms();
            if (platforms.isEmpty())
                return "No platforms registered. Risk score unavailable.";

            long highRiskCount = platforms.stream()
                    .filter(p -> "HIGH".equalsIgnoreCase(p.getRiskLevel()))
                    .count();
            long mediumRiskCount = platforms.stream()
                    .filter(p -> "MEDIUM".equalsIgnoreCase(p.getRiskLevel()))
                    .count();

            String breakdown = platforms.stream()
                    .map(p -> p.getPlatformName() + " (@" + p.getHandle() + "): "
                            + (p.getRiskLevel() != null ? p.getRiskLevel() : "Not assessed"))
                    .reduce((a, b) -> a + ", " + b).orElse("N/A");

            return "Risk Assessment — Platforms: " + platforms.size()
                 + " | High Risk: " + highRiskCount
                 + " | Medium Risk: " + mediumRiskCount
                 + " | Breakdown: " + breakdown;
        } catch (Exception e) {
            log.error("getUserRiskScore failed: {}", e.getMessage());
            return "Unable to retrieve risk score. Please try again.";
        }
    }

    @Tool("Get a summary of the authenticated user's insurance policies")
    public String getUserPolicies(Long userId) {
        try {
            List<PolicySummaryResponse> policies = policyService.getMyPolicies();
            if (policies.isEmpty()) return "No active policies found.";
            StringBuilder sb = new StringBuilder();
            for (PolicySummaryResponse p : policies) {
                sb.append("Policy #").append(p.getPolicyNumber())
                  .append(" | Status: ").append(p.getStatus())
                  .append(" | Premium: $").append(p.getPremiumAmount()).append("/mo")
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getUserPolicies failed: {}", e.getMessage());
            return "Unable to retrieve policies. Please try again.";
        }
    }

    @Tool("Get a summary of the authenticated user's filed insurance claims")
    public String getUserClaims(Long userId) {
        try {
            List<ClaimSummaryResponse> claims = claimService.getMyClaims();
            if (claims.isEmpty()) return "No filed claims found.";
            StringBuilder sb = new StringBuilder();
            for (ClaimSummaryResponse c : claims) {
                sb.append("Claim #").append(c.getClaimNumber())
                  .append(" | Status: ").append(c.getStatus())
                  .append(" | Amount: $").append(c.getClaimAmount())
                  .append(" | Date: ").append(c.getIncidentDate())
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getUserClaims failed: {}", e.getMessage());
            return "Unable to retrieve claims. Please try again.";
        }
    }
}
