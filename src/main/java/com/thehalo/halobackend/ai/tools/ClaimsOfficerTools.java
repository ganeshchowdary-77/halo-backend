package com.thehalo.halobackend.ai.tools;

import com.thehalo.halobackend.dto.claim.response.ClaimSummaryResponse;
import com.thehalo.halobackend.dto.platform.response.PlatformSummaryResponse;
import com.thehalo.halobackend.service.claim.ClaimService;
import com.thehalo.halobackend.service.user.UserPlatformService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LangChain4j tools for the CLAIMS_OFFICER role chatbot.
 * <p>
 * <strong>Read-only by design.</strong> The Claims Officer agent assists with
 * reviewing pending claims, detecting fraud patterns, and tracking personal KPIs.
 * It never approves, denies, or modifies claim records.
 * <p>
 * <strong>Refactored to use Services instead of Repositories for better architecture.</strong>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimsOfficerTools {

    private final ClaimService claimService;
    private final UserPlatformService userPlatformService;

    /** ThreadLocal for officer-scoped tools (performance metrics). */
    private final ThreadLocal<Long> currentOfficerId = new ThreadLocal<>();

    public void setOfficerId(Long officerId) { currentOfficerId.set(officerId); }
    public void clearOfficerId()             { currentOfficerId.remove(); }

    private Long getOfficerId() {
        Long id = currentOfficerId.get();
        if (id == null) throw new IllegalStateException("Officer context not set for ClaimsOfficerTools");
        return id;
    }

    @Tool("Get all claims currently pending review or under examination, with type, amount, and status")
    public String getPendingClaims() {
        List<ClaimSummaryResponse> claims = claimService.getClaimQueue();
        if (claims.isEmpty()) return "There are no claims pending review at this time.";
        
        return claims.stream()
                .map(c -> "Claim #" + c.getClaimNumber()
                        + " | User: " + c.getFiledByEmail()
                        + " | Amount: $" + c.getClaimAmount()
                        + " | Type: " + c.getExpenseType()
                        + " | Status: " + c.getStatus()
                        + " | Filed: " + c.getIncidentDate()
                        + " | Claim ID: " + c.getId())
                .collect(Collectors.joining("\n"));
    }

    @Tool("Get detailed incident data for a specific claim by claim ID: description, amount, dates, and policy info")
    public String getClaimDetails(Long claimId) {
        try {
            var claim = claimService.getDetail(claimId);
            return "Claim #" + claim.getClaimNumber() + " Details:\n"
                    + "  Status       : " + claim.getStatus() + "\n"
                    + "  Expense Type : " + claim.getExpenseType() + "\n"
                    + "  Amount       : $" + claim.getClaimAmount() + "\n"
                    + "  Incident Date: " + claim.getIncidentDate() + "\n"
                    + "  Description  : " + claim.getDescription() + "\n"
                    + "  Filed By     : " + claim.getFiledByEmail() + "\n"
                    + "  Policy       : #" + claim.getPolicyNumber();
        } catch (Exception e) {
            return "Claim not found with ID: " + claimId;
        }
    }

    @Tool("Get the full claim history for a specific user ID to assess claim frequency for fraud analysis")
    public String getUserClaimHistory(Long userId) {
        List<ClaimSummaryResponse> claims = claimService.getUserClaimHistory(userId);
        if (claims.isEmpty()) return "No claims found for user ID: " + userId;

        long flagged = claims.size() > 3 ? claims.size() : 0;
        return "Claim History for user " + userId + " (" + claims.size() + " total"
                + (flagged > 0 ? " — ⚠️ HIGH FREQUENCY FLAG" : "") + "):\n"
                + claims.stream()
                        .map(c -> "  Claim #" + c.getClaimNumber()
                                + " | $" + c.getClaimAmount()
                                + " | " + c.getStatus()
                                + " | " + c.getIncidentDate())
                        .collect(Collectors.joining("\n"));
    }

    @Tool("Get social media platform metrics for a user by userId to contextualise their claim (followers, engagement, niche)")
    public String getUserPlatformMetrics(Long userId) {
        // Note: UserPlatformService doesn't have a method to get platforms by userId
        // We'll need to add this or use a workaround
        // For now, returning a message to implement this properly
        return "Platform metrics lookup by userId needs to be implemented in UserPlatformService. "
                + "Please use the Claims Officer Portal to view platform details.";
    }

    @Tool("Search for claims by user's name (first name, last name, or full name). " +
          "Returns all claims filed by users matching the search term with claim details.")
    public String searchClaimsByUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return "Please provide a user name to search for claims.";
        }

        List<ClaimSummaryResponse> claims = claimService.searchClaimsByUserName(userName.trim());
        
        if (claims.isEmpty()) {
            return "No claims found for users with name matching: " + userName;
        }

        // Group claims by user email (as a proxy for user identity)
        Map<String, List<ClaimSummaryResponse>> claimsByUser = claims.stream()
                .collect(Collectors.groupingBy(ClaimSummaryResponse::getFiledByEmail));

        StringBuilder result = new StringBuilder();
        result.append("Found ").append(claims.size()).append(" claim(s) from ")
              .append(claimsByUser.size()).append(" user(s) matching '").append(userName).append("':\n\n");

        claimsByUser.forEach((email, userClaims) -> {
            // Get user ID from first claim
            Long userId = userClaims.get(0).getFiledById();
            
            result.append("User: ").append(email)
                  .append(" - User ID: ").append(userId)
                  .append(" - ").append(userClaims.size()).append(" claim(s)\n");
            
            userClaims.forEach(c -> {
                result.append("  • Claim #").append(c.getClaimNumber())
                      .append(" | Status: ").append(c.getStatus())
                      .append(" | Amount: $").append(c.getClaimAmount())
                      .append(" | Type: ").append(c.getExpenseType())
                      .append(" | Filed: ").append(c.getIncidentDate())
                      .append(" | Claim ID: ").append(c.getId())
                      .append("\n");
            });
            result.append("\n");
        });

        result.append("Use getUserClaimHistory(userId) for detailed history or getClaimDetails(claimId) for specific claim info.");
        
        return result.toString();
    }
}
