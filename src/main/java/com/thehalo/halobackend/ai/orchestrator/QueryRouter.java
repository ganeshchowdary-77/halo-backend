package com.thehalo.halobackend.ai.orchestrator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Query routing engine — determines whether a user message should be answered
 * via tool calls, RAG retrieval, or a combination of both.
 * <p>
 * This implements the routing logic from the spec:
 * <pre>
 *   "my", "mine", "my policy", "my risk"  → TOOL_ONLY
 *   "what is", "explain", "how does"       → RAG_ONLY
 *   Mixed personal + general questions     → COMBINED
 * </pre>
 */
@Component
@Slf4j
public class QueryRouter {

    public enum RouteType {
        TOOL_ONLY,   // Requires authenticated user data via tools
        RAG_ONLY,    // General insurance/legal knowledge from vector store
        COMBINED     // Both tool data + RAG context needed
    }

    // ── Personal (tool-required) patterns ─────────────────────────────────────
    private static final List<Pattern> PERSONAL_PATTERNS = List.of(
        Pattern.compile("\\bmy\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bmine\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bmy policy\\b|\\bmy policies\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bmy risk\\b|\\bmy risk score\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bmy claim\\b|\\bmy claims\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bmy application\\b|\\bmy applications\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bmy premium\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bmy platform\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bi have\\b|\\bi owe\\b|\\bi need to\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bshow me\\b|\\bwhat do i\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bpending (application|applications|claim|claims)\\b", Pattern.CASE_INSENSITIVE)
    );

    // ── General knowledge (RAG-required) patterns ────────────────────────────
    private static final List<Pattern> GENERAL_PATTERNS = List.of(
        Pattern.compile("\\bwhat is\\b|\\bwhat are\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bexplain\\b|\\bhow does\\b|\\bhow do\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bdefine\\b|\\bdefinition\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bwhat (does|do)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bcyber insurance\\b|\\bdefamation\\b|\\breputation\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\blegal (coverage|protection|fees)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bhow (does|do) (a |the )?claim\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\binsurance (work|cover|protect)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\bfaq\\b|\\bquestion\\b", Pattern.CASE_INSENSITIVE)
    );

    /**
     * Classify a query to determine the appropriate retrieval/execution strategy.
     *
     * @param message the sanitized user message
     * @return the routing strategy to apply
     */
    public RouteType classify(String message) {
        boolean personal = PERSONAL_PATTERNS.stream().anyMatch(p -> p.matcher(message).find());
        boolean general  = GENERAL_PATTERNS.stream().anyMatch(p -> p.matcher(message).find());

        RouteType route;
        if (personal && general) {
            route = RouteType.COMBINED;
        } else if (personal) {
            route = RouteType.TOOL_ONLY;
        } else if (general) {
            route = RouteType.RAG_ONLY;
        } else {
            // Default: let the agent decide with both capabilities available
            route = RouteType.COMBINED;
        }

        log.debug("QueryRouter: classified='{}' personal={} general={} → {}", 
                  message.substring(0, Math.min(60, message.length())), personal, general, route);
        return route;
    }
}
