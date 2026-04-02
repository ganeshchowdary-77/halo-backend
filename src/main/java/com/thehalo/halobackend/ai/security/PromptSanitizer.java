package com.thehalo.halobackend.ai.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Enterprise-grade prompt injection protection layer.
 * <p>
 * Sanitizes user input BEFORE it reaches the LLM, neutralising common
 * prompt injection attack vectors. Security is enforced even when the LLM
 * is compromised or behaves unexpectedly.
 */
@Component
@Slf4j
public class PromptSanitizer {

    private static final int MAX_MESSAGE_LENGTH = 2000;

    // ── Injection patterns to block ────────────────────────────────────────────
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
        Pattern.compile("ignore (all )?(previous |above )?instructions?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("forget (everything|all|your instructions?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("you are now", Pattern.CASE_INSENSITIVE),
        Pattern.compile("act as (a |an )?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("jailbreak", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\[INST\\]|\\[/INST\\]|\\[SYSTEM\\]", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<\\|im_start\\|>|<\\|im_end\\|>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("system prompt|system message|initial prompt", Pattern.CASE_INSENSITIVE),
        Pattern.compile("DAN mode|do anything now", Pattern.CASE_INSENSITIVE),
        Pattern.compile("pretend (you are|to be)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("repeat after me:?\\s+system", Pattern.CASE_INSENSITIVE),
        Pattern.compile("disregard (your|all) (previous )?(instructions?|training)", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Sanitize user input before forwarding to the LLM agent.
     *
     * @param rawInput the raw user message
     * @return a sanitized version safe for LLM consumption
     * @throws IllegalArgumentException if input is null or blank
     */
    public String sanitize(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        String sanitized = rawInput.strip();

        // 1. Length check
        if (sanitized.length() > MAX_MESSAGE_LENGTH) {
            log.warn("PromptSanitizer: message truncated from {} to {} chars", sanitized.length(), MAX_MESSAGE_LENGTH);
            sanitized = sanitized.substring(0, MAX_MESSAGE_LENGTH);
        }

        // 2. Injection pattern detection
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(sanitized).find()) {
                log.warn("PromptSanitizer: PROMPT INJECTION ATTEMPT detected. Pattern='{}' Input='{}'",
                        pattern.pattern(), sanitized.substring(0, Math.min(100, sanitized.length())));
                return "I'm here to help with your insurance questions. What would you like to know?";
            }
        }

        // 3. Remove control characters (null bytes, etc.)
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        return sanitized;
    }
}
