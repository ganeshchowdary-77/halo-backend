package com.thehalo.halobackend.ai.agent;

import com.thehalo.halobackend.ai.agent.base.AgentContext;
import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import com.thehalo.halobackend.enums.RoleName;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry that routes users to their role-specific AI agents.
 * 
 * This service acts as a factory and router:
 * 1. Maintains conversation memory for each user session
 * 2. Routes to the correct agent based on user role
 * 3. Ensures each user gets a personalized agent instance
 * 
 * Architecture:
 * - One agent builder per role (InfluencerAgent, UnderwriterAgent, etc.)
 * - One ChatMemory per session (stores last 25 messages)
 * - Thread-safe memory storage using ConcurrentHashMap
 * 
 * Flow:
 * AiChatService → AgentRegistry.getAgentForUser(ctx) → Role-specific Agent Builder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentRegistry {

    // Inject all role-specific agent builders
    private final InfluencerAgent    influencerAgent;
    private final UnderwriterAgent   underwriterAgent;
    private final ClaimsOfficerAgent claimsOfficerAgent;
    private final PolicyAdminAgent   policyAdminAgent;
    private final PublicAgent        publicAgent;

    // Thread-safe storage for conversation history
    // Key: sessionId (e.g., "user-123"), Value: ChatMemory with last 25 messages
    private final ConcurrentHashMap<String, ChatMemory> memoryStore = new ConcurrentHashMap<>();

    /**
     * Get the appropriate AI agent for an authenticated user.
     * 
     * This method:
     * 1. Extracts the user's role from AgentContext
     * 2. Gets or creates ChatMemory for their session
     * 3. Routes to the correct agent builder
     * 4. Returns a configured HaloAgent ready to chat
     * 
     * @param ctx contains userId, sessionId, and full AppUser
     * @return a role-specific HaloAgent configured with memory and tools
     */
    public HaloAgent getAgentForUser(AgentContext ctx) {
        RoleName role = ctx.user().getRole().getName();
        ChatMemory memory = getOrCreateMemory(ctx.sessionId());
        
        log.debug("AgentRegistry: routing role={} sessionId={}", role, ctx.sessionId());

        // Route to appropriate agent based on role
        return switch (role) {
            case INFLUENCER     -> influencerAgent.buildFor(ctx, memory);
            case UNDERWRITER    -> underwriterAgent.buildFor(ctx, memory);
            case CLAIMS_OFFICER -> claimsOfficerAgent.buildFor(ctx, memory);
            case POLICY_ADMIN   -> policyAdminAgent.buildFor(ctx, memory);
            default             -> userMessage -> "AI chat is not available for your account type.";
        };
    }

    /**
     * Get the public AI agent for unauthenticated users.
     * 
     * Used for the landing page chatbot that answers general questions
     * about products and services without requiring login.
     * 
     * @param sessionId unique session identifier (e.g., from cookie)
     * @return a public HaloAgent with limited capabilities
     */
    public HaloAgent getPublicAgent(String sessionId) {
        ChatMemory memory = getOrCreateMemory(sessionId);
        return publicAgent.buildFor(sessionId, memory);
    }

    /**
     * Get existing ChatMemory or create a new one for the session.
     * 
     * ChatMemory stores the last 25 messages (user + AI responses) to maintain
     * conversation context. This allows users to ask follow-up questions like
     * "tell me more about that" without repeating context.
     * 
     * Thread-safe: Uses ConcurrentHashMap.computeIfAbsent()
     * 
     * @param sessionId unique session identifier
     * @return ChatMemory for this session (existing or newly created)
     */
    private ChatMemory getOrCreateMemory(String sessionId) {
        return memoryStore.computeIfAbsent(
                sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(25)
        );
    }
}
