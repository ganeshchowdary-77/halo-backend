package com.thehalo.halobackend.ai.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Filters LLM output to prevent accidental leakage of sensitive data or system internals.
 * <p>
 * Applied AFTER the agent responds, BEFORE building the {@code ChatResponse} DTO.
 */
@Component
@Slf4j
public class AiOutputFilter {

    // Patterns that should never appear in output sent to the client
    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
        Pattern.compile("\\[INST\\]|\\[/INST\\]|<\\|im_start\\|>|<\\|im_end\\|>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("System:\\s*You are", Pattern.CASE_INSENSITIVE),
        Pattern.compile("app\\.ai\\.api-key|GROQ_API_KEY", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Filter the LLM's response before returning to the client.
     *
     * @param rawOutput the raw LLM output
     * @return a safe, filtered output string
     */
    public String filter(String rawOutput) {
        if (rawOutput == null) {
            return "I'm unable to provide a response at this time. Please try again.";
        }

        String filtered = rawOutput;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            if (pattern.matcher(filtered).find()) {
                log.warn("AiOutputFilter: Sensitive pattern detected in LLM output — redacting.");
                filtered = pattern.matcher(filtered).replaceAll("[REDACTED]");
            }
        }

        return filtered.strip();
    }
}
