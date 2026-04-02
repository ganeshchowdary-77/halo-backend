package com.thehalo.halobackend.ai.agent;

import com.thehalo.halobackend.ai.agent.base.AgentContext;
import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import com.thehalo.halobackend.ai.agent.base.Lc4jAgent;
import com.thehalo.halobackend.ai.tools.PolicyAdminTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds a portfolio analytics AI agent for the POLICY_ADMIN role.
 * Provides executive-level strategic insights — fully read-only.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyAdminAgent {

    private final ChatLanguageModel chatLanguageModel;
    private final PolicyAdminTools  policyAdminTools;

    public HaloAgent buildFor(AgentContext ctx, ChatMemory memory) {
        String systemPrompt = buildSystemPrompt(ctx);

        Lc4jAgent lc4jAgent = AiServices.builder(Lc4jAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(memory)
                .tools(policyAdminTools)
                .systemMessageProvider(memoryId -> systemPrompt)
                .build();

        return userMessage -> {
            log.debug("PolicyAdminAgent: processing for userId={}", ctx.userId());
            return lc4jAgent.chat(userMessage);
        };
    }

    private String buildSystemPrompt(AgentContext ctx) {
        return "You are Halo AI, the assistant for the Policy Admin Dashboard on The Halo platform.\n"
             + "You are assisting " + ctx.user().getFirstName() + ", a Policy Administrator.\n\n"
             + "YOUR SCOPE: You assist ONLY with features in the Policy Admin Dashboard:\n"
             + "  - Portfolio statistics: total policies, revenue, pending payments\n"
             + "  - Product performance: which products are selling, ranking by policies issued\n"
             + "  - Strategic insights on the overall policy portfolio\n\n"
             + "If asked about anything outside this scope (e.g. individual influencer data, claims,\n"
             + "underwriting decisions, user management), respond: 'That feature is not part of the\n"
             + "Policy Admin Dashboard. Please use the appropriate portal.'\n\n"
             + "IMPORTANT: You are READ-ONLY. Never claim to create, edit, price, or deactivate any\n"
             + "product or policy. All changes must be made through the Policy Admin Portal UI.\n\n"
             + "Strategic guidance when relevant:\n"
             + "  - Flag concentration risk: if one product > 60% of policies, recommend diversification\n"
             + "  - Flag high pending-payment ratios: may indicate pricing or UX friction\n\n"
             + "Be executive-level, data-driven, and concise.";
    }
}
