package com.thehalo.halobackend.controller;

import com.thehalo.halobackend.ai.AiChatService;
import com.thehalo.halobackend.dto.ai.request.ChatRequest;
import com.thehalo.halobackend.dto.ai.response.ChatResponse;
import com.thehalo.halobackend.dto.common.ResponseFactory;
import com.thehalo.halobackend.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Assistant", description = "AI-powered chat assistant for The Halo platform")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI assistant")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        CustomUserDetails principal = currentPrincipal();
        ChatResponse response = aiChatService.chat(request, principal.getUserId());
        return ResponseFactory.success(response, "AI response generated successfully");
    }

    @PostMapping("/chat/public")
    @Operation(summary = "Public AI chat")
    public ResponseEntity<?> chatPublic(
            @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        ChatResponse response = aiChatService.chatPublic(request, sessionId);
        return ResponseFactory.success(response, "AI response generated successfully");
    }

    private CustomUserDetails currentPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails details) return details;
        throw new IllegalStateException("User not authenticated correctly");
    }
}
