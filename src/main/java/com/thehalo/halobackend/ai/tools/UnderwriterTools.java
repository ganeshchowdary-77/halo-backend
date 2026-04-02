package com.thehalo.halobackend.ai.tools;

import com.thehalo.halobackend.enums.PolicyStatus;
import com.thehalo.halobackend.model.policy.PolicyApplication;
import com.thehalo.halobackend.repository.PolicyApplicationRepository;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LangChain4j tools for the UNDERWRITER role chatbot.
 * Read-only analytical tools — no state mutations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnderwriterTools {

    private final PolicyApplicationRepository applicationRepository;

    @Tool("Get all policy applications currently assigned to the underwriter for review")
    public String getAssignedApplications() {
        try {
            List<PolicyApplication> apps = applicationRepository.findByStatus(PolicyStatus.UNDER_REVIEW);
            if (apps.isEmpty()) return "No applications currently assigned for review.";

            int i = 1;
            StringBuilder sb = new StringBuilder();
            for (PolicyApplication a : apps) {
                sb.append(i++).append(". Application #").append(a.getApplicationNumber())
                  .append(" | Product: ").append(a.getProduct().getName())
                  .append(" | Influencer: ").append(a.getUser().getFullName())
                  .append(" | Platform: ").append(a.getProfile().getPlatform().getName())
                  .append(" | Followers: ").append(String.format("%,d", a.getProfile().getFollowerCount()))
                  .append(" | Risk Score: ").append(a.getRiskScore() != null ? a.getRiskScore() : "N/A")
                  .append(" | Premium: $").append(a.getCalculatedPremium())
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("getAssignedApplications failed: {}", e.getMessage());
            return "Unable to retrieve assigned applications.";
        }
    }

    @Tool("Get summary statistics of policy applications by status")
    public String getApplicationStats() {
        try {
            long underReview = applicationRepository.countByStatus(PolicyStatus.UNDER_REVIEW);
            long approved = applicationRepository.countByStatus(PolicyStatus.PENDING_PAYMENT);
            long rejected = applicationRepository.countByStatus(PolicyStatus.APPLICATION_REJECTED);
            long total = applicationRepository.count();

            return String.format(
                "Application Statistics:\n" +
                "  Under Review: %d\n" +
                "  Approved (Pending Payment): %d\n" +
                "  Rejected: %d\n" +
                "  Total: %d",
                underReview, approved, rejected, total);
        } catch (Exception e) {
            log.error("getApplicationStats failed: {}", e.getMessage());
            return "Unable to retrieve application statistics.";
        }
    }
}
