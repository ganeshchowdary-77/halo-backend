package com.thehalo.halobackend.ai.agent.base;

/**
 * Functional interface representing a role-specific AI agent.
 * 
 * This is the public API that all agent builders return. It hides the complexity
 * of LangChain4j, tools, and memory management behind a simple chat() method.
 * 
 * Each concrete agent builder (InfluencerAgent, UnderwriterAgent, etc.) returns
 * a HaloAgent lambda that encapsulates:
 * - Role-specific system prompt
 * - Appropriate tool set
 * - Conversation memory
 * - User context (via ThreadLocal)
 * 
 * Callers only see: agent.chat(userMessage) → response
 */
@FunctionalInterface
public interface HaloAgent {
    
    /**
     * Process a user message and return the AI-generated response.
     * 
     * This method:
     * 1. Sets user context in ThreadLocal
     * 2. Sends message to LangChain4j agent
     * 3. AI may call tools to fetch data
     * 4. Returns natural language response
     * 5. Clears ThreadLocal context
     *
     * @param userMessage the raw user message text
     * @return the AI-generated response string
     */
    String chat(String userMessage);
}
