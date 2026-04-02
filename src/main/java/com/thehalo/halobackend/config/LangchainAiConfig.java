package com.thehalo.halobackend.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures both the synchronous and streaming LangChain4j model beans
 * that back all Halo AI agents.
 * <p>
 * <strong>Provider:</strong> Groq API (OpenAI-compatible) via {@code OpenAiChatModel}.<br>
 * <strong>Model:</strong> {@code llama3-70b-8192} — best Groq model for structured tool-calling.<br>
 * <strong>Temperature:</strong> 0.2 — low for consistent, professional responses.
 */
@Configuration
@Slf4j
public class LangchainAiConfig {

    /**
     * Synchronous chat model used by all role-specific agents for standard chat.
     */
    @Bean
    public ChatLanguageModel chatLanguageModel(AiProviderConfig aiConfig) {
        validate(aiConfig);

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  Halo AI — Synchronous Model Initialised              ║");
        log.info("║  Provider : Groq (OpenAI-compatible)                  ║");
        log.info("║  Model    : {}                        ║", aiConfig.getModel());
        log.info("║  Temp     : {}  │  MaxTokens: {}               ║",
                aiConfig.getTemperature(), aiConfig.getMaxTokens());
        log.info("╚══════════════════════════════════════════════════════╝");

        return OpenAiChatModel.builder()
                .baseUrl(aiConfig.getBaseUrl())
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModel())
                .temperature(aiConfig.getTemperature())
                .maxTokens(aiConfig.getMaxTokens())
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * Streaming chat model used by {@code AiStreamController} for SSE (Server-Sent Events).
     * Uses the same Groq endpoint with identical configuration.
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel(AiProviderConfig aiConfig) {
        validate(aiConfig);

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║  Halo AI — Streaming Model Initialised                ║");
        log.info("║  Provider : Groq (OpenAI-compatible SSE)              ║");
        log.info("║  Model    : {}                        ║", aiConfig.getModel());
        log.info("╚══════════════════════════════════════════════════════╝");

        return OpenAiStreamingChatModel.builder()
                .baseUrl(aiConfig.getBaseUrl())
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModel())
                .temperature(aiConfig.getTemperature())
                .maxTokens(aiConfig.getMaxTokens())
                .build();
    }

    private void validate(AiProviderConfig aiConfig) {
        if (!aiConfig.isConfigured()) {
            throw new IllegalStateException(
                    "[Halo AI] Groq API key is not configured. " +
                    "Set the GROQ_API_KEY environment variable or app.ai.api-key in application.properties.");
        }
    }
}
