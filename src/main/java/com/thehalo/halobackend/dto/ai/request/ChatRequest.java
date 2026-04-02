package com.thehalo.halobackend.dto.ai.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private List<ChatHistoryItem> conversationHistory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryItem {
        private String role; // "user" or "assistant"
        private String content;
    }
}
