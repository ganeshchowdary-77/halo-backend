package com.thehalo.halobackend.ai.agent.base;

import dev.langchain4j.service.UserMessage;

/**
 * Internal LangChain4j AiServices interface used by all role-specific agent builders.
 * 
 * IMPORTANT DESIGN NOTE:
 * This interface intentionally has NO @SystemMessage annotation. Instead, each agent
 * injects its personalized system prompt dynamically via:
 * 
 *   AiServices.builder()
 *     .systemMessageProvider(memoryId -> systemPrompt)
 *     .build()
 * 
 * Why this approach?
 * - The old pattern of @SystemMessage("{{sys}}") + @V("sys") parameter interfered
 *   with tool-calling because it passed the system prompt as a method argument
 * - This broke the OpenAI/Groq function-calling contract
 * - The systemMessageProvider() API is the correct way to inject dynamic system messages
 * - It keeps the user message clean and separate from system instructions
 * 
 * This simpler interface allows LangChain4j to correctly format the API call with
 * proper role separation: system / user / assistant / tool
 */
public interface Lc4jAgent {

    /**
     * Send a user message to the AI model (Groq).
     * 
     * The system prompt is injected separately via systemMessageProvider(),
     * not as a method parameter.
     *
     * @param message the raw user message text
     * @return the AI model's response
     */
    String chat(@UserMessage String message);
}
