package com.thehalo.halobackend.ai;

import com.thehalo.halobackend.ai.agent.AgentRegistry;
import com.thehalo.halobackend.ai.agent.base.AgentContext;
import com.thehalo.halobackend.ai.agent.base.HaloAgent;
import com.thehalo.halobackend.ai.orchestrator.QueryRouter;
import com.thehalo.halobackend.ai.security.AiOutputFilter;
import com.thehalo.halobackend.ai.security.PromptSanitizer;
import com.thehalo.halobackend.dto.ai.request.ChatRequest;
import com.thehalo.halobackend.dto.ai.response.ChatResponse;
import com.thehalo.halobackend.model.user.AppUser;
import com.thehalo.halobackend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main service orchestrating AI chat interactions for authenticated and public users.
 * 
 * This service is the entry point for all AI chat requests. It handles:
 * 1. Security: Input sanitization and output filtering
 * 2. User Context: Loading user data and creating AgentContext
 * 3. Routing: Delegating to AgentRegistry to get the right agent
 * 4. Error Handling: Graceful fallbacks for any failures
 * 
 * Flow for authenticated users:
 * Controller → AiChatService.chat() → AgentRegistry → Role-specific Agent → Tools → Response
 * 
 * Flow for public users:
 * Controller → AiChatService.chatPublic() → AgentRegistry → PublicAgent → Response
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final AgentRegistry     agentRegistry;
    private final AppUserRepository userRepository;
    private final PromptSanitizer   promptSanitizer;
    private final AiOutputFilter    outputFilter;
    private final QueryRouter       queryRouter;

    /**
     * Process a chat message from an authenticated user.
     * 
     * Steps:
     * 1. Load user from database
     * 2. Sanitize input (remove injection attempts)
     * 3. Classify query type (optional, for analytics)
     * 4. Create AgentContext with user info
     * 5. Get role-specific agent from registry
     * 6. Send message to agent (may trigger tool calls)
     * 7. Filter output (remove PII/sensitive data)
     * 8. Return ChatResponse
     * 
     * @param request contains the user's message
     * @param userId  the authenticated user's ID (from JWT token)
     * @return ChatResponse with AI-generated message
     */
    public ChatResponse chat(ChatRequest request, Long userId) {
        // Load user entity for personalization and role routing
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Sanitize input to prevent prompt injection, XSS, SQL injection
        String safeMessage;
        try {
            safeMessage = promptSanitizer.sanitize(request.getMessage());
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Message cannot be empty.");
        }

        // Classify query type (for analytics/routing - currently unused)
        QueryRouter.RouteType routeType = queryRouter.classify(safeMessage);
        log.debug("AiChatService: userId={} role={} route={}", userId, user.getRole().getName(), routeType);

        // Create context with user info for agent building
        AgentContext ctx = AgentContext.of(user);

        try {
            // Get role-specific agent (Influencer, Underwriter, etc.)
            HaloAgent agent = agentRegistry.getAgentForUser(ctx);
            
            // Send message to agent - this may trigger tool calls
            String rawResponse = agent.chat(safeMessage);
            
            // Filter output to remove any PII or sensitive data
            String filtered = outputFilter.filter(rawResponse);

            return ChatResponse.builder()
                    .message(filtered)
                    .sources(List.of("Halo AI — " + user.getRole().getName()))
                    .timestamp(LocalDateTime.now())
                    .aiGenerated(true)
                    .build();

        } catch (Exception e) {
            log.error("AiChatService: error for userId={} role={}: {}", 
                     userId, user.getRole().getName(), e.getMessage(), e);
            return buildFallbackResponse();
        }
    }

    /**
     * Process a chat message from an unauthenticated public user.
     * 
     * Used for the landing page chatbot. Provides general information about
     * products and services without requiring login.
     * 
     * @param request   contains the user's message
     * @param sessionId unique session identifier (from cookie or generated)
     * @return ChatResponse with AI-generated message
     */
    public ChatResponse chatPublic(ChatRequest request, String sessionId) {
        // Sanitize input
        String safeMessage;
        try {
            safeMessage = promptSanitizer.sanitize(request.getMessage());
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Message cannot be empty.");
        }

        // Use provided session ID or generate default
        String sid = (sessionId != null && !sessionId.isBlank()) ? sessionId : "public-session";

        try {
            // Get public agent (limited capabilities, no user-specific data)
            HaloAgent agent = agentRegistry.getPublicAgent(sid);
            String rawResponse = agent.chat(safeMessage);
            String filtered = outputFilter.filter(rawResponse);

            return ChatResponse.builder()
                    .message(filtered)
                    .sources(List.of("Halo AI — Public"))
                    .timestamp(LocalDateTime.now())
                    .aiGenerated(true)
                    .build();

        } catch (Exception e) {
            log.error("AiChatService: public error: {}", e.getMessage(), e);
            return buildFallbackResponse();
        }
    }

    /**
     * Build a generic fallback response when AI fails.
     * Never exposes internal errors to users.
     */
    private ChatResponse buildFallbackResponse() {
        return ChatResponse.builder()
                .message("I'm sorry, I'm having trouble right now. Please try again in a moment.")
                .sources(List.of("Fallback"))
                .timestamp(LocalDateTime.now())
                .aiGenerated(false)
                .build();
    }

    /**
     * Build an error response for validation failures.
     */
    private ChatResponse buildErrorResponse(String message) {
        return ChatResponse.builder()
                .message(message)
                .sources(List.of("Validation"))
                .timestamp(LocalDateTime.now())
                .aiGenerated(false)
                .build();
    }
}
