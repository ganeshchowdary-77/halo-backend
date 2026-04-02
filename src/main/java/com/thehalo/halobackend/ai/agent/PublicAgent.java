package com.thehalo.halobackend.ai.agent;

import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import com.thehalo.halobackend.ai.agent.base.Lc4jAgent;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Public chatbot — strictly RAG-only, NO tools, NO user data access.
 *
 * Unauthenticated visitors can ask about products, coverage, and general
 * insurance questions. Answers come from the knowledge base (RAG) only.
 * Any request for personal data, quoting, or account actions is declined
 * and the user is directed to register/log in.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PublicAgent {

    private final ChatLanguageModel chatLanguageModel;
    private final ContentRetriever  contentRetriever;

    public HaloAgent buildFor(String sessionId, ChatMemory memory) {
        String systemPrompt =
                "You are Halo AI, the public assistant for The Halo platform — "
              + "an insurance provider specialising in social media influencer coverage.\n\n"
              + "YOUR ROLE:\n"
              + "You help prospective visitors learn about our insurance products and services.\n"
              + "Answer questions based on the provided knowledge base (context documents).\n\n"
              + "YOU CAN HELP WITH:\n"
              + "  - What insurance plans we offer (Halo Basic, Halo Plus, Halo Elite, etc.)\n"
              + "  - What defamation, reputation, and cyber insurance covers\n"
              + "  - How our underwriting and claims process works\n"
              + "  - Eligibility, platform types we support\n"
              + "  - General questions about The Halo platform\n\n"
              + "YOU CANNOT AND WILL NOT:\n"
              + "  - Access any user account, application, policy, or claim data\n"
              + "  - Call any tools or fetch any database records\n"
              + "  - Provide a personalised premium or risk assessment (redirect to register)\n"
              + "  - Discuss any internal system details\n\n"
              + "If asked for anything personal or account-related, respond:\n"
              + "  'I'm the public assistant and can't access account data. "
              + "Please log in to your dashboard for personalised assistance, "
              + "or register at thehalo.com to get started.'\n\n"
              + "Be welcoming, clear, and concise. Always encourage visitors to sign up.";

        // NO .tools() — this agent is strictly RAG + general knowledge only
        Lc4jAgent lc4jAgent = AiServices.builder(Lc4jAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(memory)
                .contentRetriever(contentRetriever)
                .systemMessageProvider(memoryId -> systemPrompt)
                .build();

        return userMessage -> {
            log.debug("PublicAgent: sessionId={}", sessionId);
            return lc4jAgent.chat(userMessage);
        };
    }
}
