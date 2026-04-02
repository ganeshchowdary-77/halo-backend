package com.thehalo.halobackend.ai.agent;

import com.thehalo.halobackend.ai.agent.base.AgentContext;
import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import com.thehalo.halobackend.ai.agent.base.Lc4jAgent;
import com.thehalo.halobackend.ai.tools.UnderwriterTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Builds a professional risk-analysis AI agent for the UNDERWRITER role.
 * Updated for the new simplified flow — no queue, auto-assigned applications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnderwriterAgent {

    private final ChatLanguageModel chatLanguageModel;
    private final UnderwriterTools  underwriterTools;

    public HaloAgent buildFor(AgentContext ctx, ChatMemory memory) {
        String systemPrompt = buildSystemPrompt(ctx);

        Lc4jAgent lc4jAgent = AiServices.builder(Lc4jAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(memory)
                .tools(underwriterTools)
                .systemMessageProvider(memoryId -> systemPrompt)
                .build();

        return userMessage -> {
            log.debug("UnderwriterAgent: processing for userId={}", ctx.userId());
            return lc4jAgent.chat(userMessage);
        };
    }

    private String buildSystemPrompt(AgentContext ctx) {
        return "You are Halo AI, the assistant for the Underwriter Dashboard on The Halo platform.\n"
             + "You are assisting " + ctx.user().getFirstName() + ", a licensed Underwriter.\n\n"
             + "YOUR SCOPE: You assist ONLY with features in the Underwriter Dashboard:\n"
             + "  - Reviewing high-risk policy applications auto-assigned to you\n"
             + "  - Analysing risk profiles for specific applications\n"
             + "  - Viewing application statistics and history\n"
             + "  - Managing risk parameters\n\n"
             + "If asked about anything outside this scope (e.g. influencer account details, claims,\n"
             + "admin functions, policy creation), respond: 'That feature is not part of the Underwriter\n"
             + "Dashboard. Please use the appropriate portal.'\n\n"
             + "IMPORTANT: You are READ-ONLY. Never claim to approve, reject, or modify any record.\n"
             + "Always direct the underwriter to take action through the Underwriter Portal UI.\n\n"
             + "Risk guidance:\n"
             + "  - High followers + controversial niche = elevated risk → recommend closer review\n"
             + "  - No 2FA + high engagement = security risk flag\n"
             + "  - Third-party management + frequent sponsored content = higher exposure\n\n"
             + "Be precise, analytical, and professional.";
    }
}
