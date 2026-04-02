package com.thehalo.halobackend.ai.agent;

import com.thehalo.halobackend.ai.agent.base.AgentContext;
import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import com.thehalo.halobackend.ai.agent.base.Lc4jAgent;
import com.thehalo.halobackend.ai.tools.InfluencerTools;
import com.thehalo.halobackend.ai.tools.HaloAiToolService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds a personalised AI agent for the INFLUENCER role.
 * Updated for the new direct policy application flow (no quotes).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InfluencerAgent {

    private final ChatLanguageModel chatLanguageModel;
    private final InfluencerTools   influencerTools;
    private final HaloAiToolService haloAiToolService;
    private final ContentRetriever  contentRetriever;

    public HaloAgent buildFor(AgentContext ctx, ChatMemory memory) {
        String systemPrompt = buildSystemPrompt(ctx);

        Lc4jAgent lc4jAgent = AiServices.builder(Lc4jAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(memory)
                .tools(influencerTools, haloAiToolService)
                .contentRetriever(contentRetriever)
                .systemMessageProvider(memoryId -> systemPrompt)
                .build();

        return userMessage -> {
            influencerTools.setUserId(ctx.userId());
            haloAiToolService.setUserId(ctx.userId());
            try {
                log.debug("InfluencerAgent: processing for userId={}", ctx.userId());
                return lc4jAgent.chat(userMessage);
            } finally {
                influencerTools.clearUserId();
                haloAiToolService.clearUserId();
            }
        };
    }

    private String buildSystemPrompt(AgentContext ctx) {
        String firstName = ctx.user().getFirstName();

        return "You are Halo AI, assistant for " + firstName + " (Influencer Dashboard).\n\n"
             + "SCOPE: Platform management, policy applications, viewing policies/claims, product info.\n"
             + "Out of scope → 'Contact support or visit the relevant portal.'\n\n"
             + "TOOLS:\n"
             + "- getMyPlatformsAndRiskScores, getAvailableInsuranceProducts\n"
             + "- getMyApplications, getMyPolicies, getMyClaims\n"
             + "- applyForPolicy (requires ALL 6 parameters)\n\n"
             + "POLICY APPLICATION - MUST COLLECT ALL 6 ANSWERS:\n"
             + "1. Platform → call getMyPlatformsAndRiskScores, show list, ask which\n"
             + "2. Product → call getAvailableInsuranceProducts, show list, ask which\n"
             + "3. Ask: '2FA enabled? (yes/no)'\n"
             + "4. Ask: 'Password rotation? (NEVER/MONTHLY/YEARLY)'\n"
             + "5. Ask: 'Third-party tools? (yes/no)'\n"
             + "6. Ask: 'Sponsored content? (NONE/OCCASIONAL/FREQUENT)'\n\n"
             + "CRITICAL: Ask ONE question per message. Do NOT call applyForPolicy until ALL 6 answers collected.\n"
             + "After all answers → call applyForPolicy(productId, platformId, hasTwoFactorAuth, passwordRotationFrequency, thirdPartyManagement, sponsoredContentFrequency)\n\n"
             + "Low-risk = auto-approved. High-risk = underwriter review.\n"
             + "Be warm, concise.";
    }
}
