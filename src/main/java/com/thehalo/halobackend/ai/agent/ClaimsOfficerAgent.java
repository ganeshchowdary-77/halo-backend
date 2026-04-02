package com.thehalo.halobackend.ai.agent;

import com.thehalo.halobackend.ai.agent.base.AgentContext;
import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import com.thehalo.halobackend.ai.agent.base.Lc4jAgent;
import com.thehalo.halobackend.ai.tools.ClaimsOfficerTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds a claims intelligence AI agent for the CLAIMS_OFFICER role.
 * Assists officers in reviewing claims, detecting fraud, and tracking KPIs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimsOfficerAgent {

    private final ChatLanguageModel  chatLanguageModel;
    private final ClaimsOfficerTools claimsOfficerTools;

    public HaloAgent buildFor(AgentContext ctx, ChatMemory memory) {
        String systemPrompt = buildSystemPrompt(ctx);

        Lc4jAgent lc4jAgent = AiServices.builder(Lc4jAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(memory)
                .tools(claimsOfficerTools)
                .systemMessageProvider(memoryId -> systemPrompt)
                .build();

        return userMessage -> {
            claimsOfficerTools.setOfficerId(ctx.userId());
            try {
                log.debug("ClaimsOfficerAgent: processing for officerId={}", ctx.userId());
                return lc4jAgent.chat(userMessage);
            } finally {
                claimsOfficerTools.clearOfficerId();
            }
        };
    }

    private String buildSystemPrompt(AgentContext ctx) {
        return "You are Halo AI, the assistant for the Claims Officer Dashboard on The Halo platform.\n"
             + "You are assisting " + ctx.user().getFirstName() + ", a Claims Officer.\n\n"
             + "YOUR SCOPE: You assist ONLY with features in the Claims Officer Dashboard:\n"
             + "  - Reviewing pending and active claims\n"
             + "  - Searching for claims by user name\n"
             + "  - Analysing incident details for a specific claim\n"
             + "  - Checking claim history for fraud detection\n"
             + "  - Viewing platform metrics to contextualise a claim\n\n"
             + "TOOLS AVAILABLE:\n"
             + "  - getPendingClaims() : all claims pending review\n"
             + "  - searchClaimsByUserName(userName) : find claims by user's first/last name\n"
             + "  - getClaimDetails(claimId) : detailed info for a specific claim\n"
             + "  - getUserClaimHistory(userId) : all claims from a specific user\n"
             + "  - getUserPlatformMetrics(userId) : social media metrics for context\n\n"
             + "If asked about anything outside this scope (e.g. applications, policies, underwriting,\n"
             + "account management), respond: 'That feature is not part of the Claims Officer\n"
             + "Dashboard. Please use the appropriate portal.'\n\n"
             + "IMPORTANT: You are READ-ONLY. Never claim to approve, deny, or close any claim.\n"
             + "Always direct the officer to the Claims Officer Portal for actions.\n\n"
             + "Fraud flags to mention when relevant:\n"
             + "  - HIGH frequency: more than 3 claims in 12 months from the same user\n"
             + "  - DISPROPORTIONATE: claim amount > 30% of estimated annual premium revenue\n"
             + "  - INCONSISTENT: expense types not matching the user's niche and platform\n\n"
             + "Be thorough, objective, and analytical.";
    }
}
