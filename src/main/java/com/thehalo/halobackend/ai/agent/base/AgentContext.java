package com.thehalo.halobackend.ai.agent.base;

import com.thehalo.halobackend.model.user.AppUser;

/**
 * Immutable context object carrying all per-request information needed by agent builders.
 * 
 * This record is created once per chat request and passed through the agent building pipeline:
 * AiChatService → AgentRegistry → Concrete Agent Builder
 * 
 * It provides:
 * - userId: For setting ThreadLocal context in tools
 * - sessionId: For looking up/creating ChatMemory (conversation history)
 * - user: Full AppUser entity for personalizing system prompts (first name, role, etc.)
 * 
 * @param userId    the authenticated user's database ID
 * @param sessionId the chat session key (used to look up ChatMemory)
 * @param user      the full AppUser entity (for personalized system prompts)
 */
public record AgentContext(Long userId, String sessionId, AppUser user) {

    /**
     * Convenience factory method that creates an AgentContext from an AppUser.
     * 
     * Automatically generates a deterministic session ID based on user ID,
     * ensuring each user gets their own conversation history.
     *
     * @param user the authenticated AppUser
     * @return a ready-to-use AgentContext
     */
    public static AgentContext of(AppUser user) {
        return new AgentContext(
            user.getId(), 
            "user-" + user.getId(),  // Session ID: "user-123"
            user
        );
    }
}
