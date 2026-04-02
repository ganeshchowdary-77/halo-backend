package com.thehalo.halobackend.ai;

import com.thehalo.halobackend.config.AiProviderConfig;
import com.thehalo.halobackend.dto.ai.response.MultiplierSuggestionResponse;
import com.thehalo.halobackend.dto.ai.response.RiskNarrativeResponse;
import com.thehalo.halobackend.exception.domain.policy.ApplicationNotFoundException;
import com.thehalo.halobackend.model.policy.PolicyApplication;
import com.thehalo.halobackend.model.user.UserPlatform;
import com.thehalo.halobackend.repository.PolicyApplicationRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * AI-powered risk analysis service used by the Underwriter workflow.
 * <p>
 * Uses LangChain4j's {@link ChatLanguageModel} (Groq / llama3-70b-8192) directly —
 * no RestTemplate, no raw HTTP. This is deliberately separate from the agentic
 * chat system: it generates structured analytical outputs, not conversational replies.
 * <p>
 * <strong>Security:</strong> Only aggregated, non-PII application/profile data is sent to the LLM.
 * No user passwords, payment details, or personal identifiers are included in prompts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiRiskService {

    private final ChatLanguageModel chatLanguageModel;
    private final PolicyApplicationRepository applicationRepository;
    private final AiProviderConfig aiConfig;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generate an AI-powered risk narrative for a specific policy application.
     */
    public RiskNarrativeResponse generateRiskNarrative(Long applicationId) {
        PolicyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found: " + applicationId));

        if (!aiConfig.isConfigured()) {
            return buildFallbackNarrative(application);
        }

        try {
            String systemPrompt = buildRiskNarrativeSystemPrompt();
            String userContext  = buildApplicationContext(application);
            String fullPrompt   = systemPrompt + "\n\n" + userContext;

            String aiResponse = chatLanguageModel.generate(fullPrompt);
            return parseRiskNarrative(aiResponse, application);

        } catch (Exception e) {
            log.error("AiRiskService: narrative generation failed for applicationId={} — {}", applicationId, e.getMessage());
            return buildFallbackNarrative(application);
        }
    }

    /**
     * Suggest an optimal multiplier value for a risk parameter.
     */
    public MultiplierSuggestionResponse suggestMultiplier(String paramKey, String description) {
        if (!aiConfig.isConfigured()) {
            return buildFallbackSuggestion(paramKey);
        }

        try {
            String prompt = buildMultiplierSystemPrompt()
                    + "\n\nParameter Key: " + paramKey
                    + "\nDescription: " + (description != null ? description : "No description provided")
                    + "\n\nPlease suggest the optimal multiplier value for this risk parameter.";

            String aiResponse = chatLanguageModel.generate(prompt);
            return parseMultiplierSuggestion(aiResponse, paramKey);

        } catch (Exception e) {
            log.error("AiRiskService: multiplier suggestion failed for paramKey={} — {}", paramKey, e.getMessage());
            return buildFallbackSuggestion(paramKey);
        }
    }

    // ── System Prompts ────────────────────────────────────────────────────────

    private String buildRiskNarrativeSystemPrompt() {
        return """
                You are a senior insurance risk analyst for The Halo, a social media influencer insurance platform.
                
                Analyse the provided application data and generate a structured risk assessment.
                
                Your response MUST follow this EXACT format:
                
                NARRATIVE:
                [Write 2-3 paragraphs analysing the risk profile. Be specific about the influencer's metrics.]
                
                RISK_FACTORS:
                - [FACTOR_NAME] | [SEVERITY: LOW/MEDIUM/HIGH] | [Brief description]
                - [FACTOR_NAME] | [SEVERITY: LOW/MEDIUM/HIGH] | [Brief description]
                - [FACTOR_NAME] | [SEVERITY: LOW/MEDIUM/HIGH] | [Brief description]
                
                RECOMMENDATION: [APPROVE / APPROVE_WITH_CONDITIONS / MANUAL_REVIEW / DECLINE]
                
                CONFIDENCE: [HIGH / MEDIUM / LOW]
                
                Be analytical and data-driven. Reference specific numbers. Do NOT reveal any PII.
                """;
    }

    private String buildMultiplierSystemPrompt() {
        return """
                You are an actuarial specialist for The Halo, an influencer insurance platform.
                
                Multiplier scale: 1.0 = baseline | >1.0 = higher risk | <1.0 = lower risk | Range: 0.5–3.0
                
                Common parameter categories:
                - PLATFORM_* (e.g., PLATFORM_TIKTOK) — platform risk
                - NICHE_* (e.g., NICHE_CRYPTO) — content niche risk
                - FOLLOWER_* — follower bracket risk
                - ENGAGEMENT_* — engagement rate risk
                
                Your response MUST follow this EXACT format:
                
                MULTIPLIER: [number between 0.5 and 3.0]
                REASONING: [2-3 sentences explaining the value]
                BENCHMARK: [Brief industry comparison]
                CONFIDENCE: [HIGH / MEDIUM / LOW]
                """;
    }

    // ── Context Builders ──────────────────────────────────────────────────────

    private String buildApplicationContext(PolicyApplication application) {
        UserPlatform profile = application.getProfile();
        StringBuilder sb = new StringBuilder("## Application Risk Data\n");
        sb.append("- Application Number: ").append(application.getApplicationNumber()).append("\n");
        sb.append("- Product        : ").append(application.getProduct() != null ? application.getProduct().getName() : "N/A").append("\n");
        sb.append("- Platform       : ").append(profile.getPlatform().getName()).append("\n");
        sb.append("- Followers      : ").append(profile.getFollowerCount()).append("\n");
        sb.append("- Engagement Rate: ").append(profile.getEngagementRate()).append("%\n");
        sb.append("- Content Niche  : ").append(profile.getNiche()).append("\n");
        sb.append("- Account Verified: ").append(profile.getVerified()).append("\n");
        sb.append("- 2FA Enabled    : ").append(application.getHasTwoFactorAuth()).append("\n");
        sb.append("- Third-Party Mgmt: ").append(application.getThirdPartyManagement()).append("\n");
        sb.append("- Sponsored Freq : ").append(application.getSponsoredContentFrequency()).append("\n");
        if (application.getRiskScore() != null)
            sb.append("- System Risk Score: ").append(application.getRiskScore()).append("/100\n");
        if (application.getCalculatedPremium() != null)
            sb.append("- Calculated Premium: $").append(application.getCalculatedPremium()).append("/month\n");
        if (application.getProduct() != null && application.getProduct().getCoverageAmount() != null)
            sb.append("- Coverage Amount: $").append(application.getProduct().getCoverageAmount()).append("\n");
        return sb.toString();
    }

    // ── Response Parsers ──────────────────────────────────────────────────────

    private RiskNarrativeResponse parseRiskNarrative(String aiResponse, PolicyApplication application) {
        String narrative       = extractSection(aiResponse, "NARRATIVE:");
        String recommendation  = extractValue(aiResponse, "RECOMMENDATION:");
        String confidence      = extractValue(aiResponse, "CONFIDENCE:");
        List<RiskNarrativeResponse.RiskFactor> factors = extractRiskFactors(aiResponse);

        return RiskNarrativeResponse.builder()
                .narrative(narrative != null ? narrative.trim() : "AI analysis completed.")
                .riskFactors(factors)
                .recommendation(recommendation != null ? recommendation.trim() : "MANUAL_REVIEW")
                .confidenceLevel(confidence != null ? confidence.trim() : "MEDIUM")
                .aiGenerated(true)
                .build();
    }

    private MultiplierSuggestionResponse parseMultiplierSuggestion(String aiResponse, String paramKey) {
        String multiplierStr = extractValue(aiResponse, "MULTIPLIER:");
        String reasoning     = extractValue(aiResponse, "REASONING:");
        String benchmark     = extractValue(aiResponse, "BENCHMARK:");
        String confidence    = extractValue(aiResponse, "CONFIDENCE:");

        double multiplier = 1.0;
        try {
            if (multiplierStr != null)
                multiplier = Math.max(0.5, Math.min(3.0, Double.parseDouble(multiplierStr.trim())));
        } catch (NumberFormatException e) {
            log.warn("AiRiskService: Failed to parse multiplier '{}'", multiplierStr);
        }

        return MultiplierSuggestionResponse.builder()
                .suggestedMultiplier(multiplier)
                .reasoning(reasoning != null ? reasoning.trim() : "Based on industry analysis.")
                .industryBenchmark(benchmark != null ? benchmark.trim() : "No benchmark available.")
                .confidenceLevel(confidence != null ? confidence.trim() : "MEDIUM")
                .aiGenerated(true)
                .build();
    }

    // ── Text Parsing Helpers ──────────────────────────────────────────────────

    private String extractSection(String text, String header) {
        int start = text.indexOf(header);
        if (start == -1) return null;
        start += header.length();
        String[] nextHeaders = {"RISK_FACTORS:", "RECOMMENDATION:", "CONFIDENCE:", "MULTIPLIER:", "REASONING:", "BENCHMARK:"};
        int end = text.length();
        for (String h : nextHeaders) {
            int idx = text.indexOf(h, start);
            if (idx != -1 && idx < end) end = idx;
        }
        return text.substring(start, end).trim();
    }

    private String extractValue(String text, String label) {
        int start = text.indexOf(label);
        if (start == -1) return null;
        start += label.length();
        int end = text.indexOf("\n", start);
        return text.substring(start, end == -1 ? text.length() : end).trim();
    }

    private List<RiskNarrativeResponse.RiskFactor> extractRiskFactors(String text) {
        List<RiskNarrativeResponse.RiskFactor> factors = new ArrayList<>();
        String section = extractSection(text, "RISK_FACTORS:");
        if (section == null) return factors;
        for (String line : section.split("\n")) {
            line = line.trim();
            if (line.startsWith("- ") || line.startsWith("* ")) {
                line = line.substring(2);
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    factors.add(RiskNarrativeResponse.RiskFactor.builder()
                            .factor(parts[0].trim())
                            .severity(parts[1].trim().replaceAll("[^A-Z]", ""))
                            .description(parts[2].trim())
                            .build());
                } else if (parts.length >= 1) {
                    factors.add(RiskNarrativeResponse.RiskFactor.builder()
                            .factor(parts[0].trim()).severity("MEDIUM").description(line).build());
                }
            }
        }
        return factors;
    }

    // ── Fallback Responses ────────────────────────────────────────────────────

    private RiskNarrativeResponse buildFallbackNarrative(PolicyApplication application) {
        UserPlatform profile = application.getProfile();
        String narrative = String.format(
                "Application for a %s influencer with %,d followers and %.1f%% engagement in the %s niche. " +
                "System risk score: %s/100. AI analysis temporarily unavailable.",
                profile.getPlatform().getName(), profile.getFollowerCount(),
                profile.getEngagementRate(), profile.getNiche(),
                application.getRiskScore() != null ? application.getRiskScore() : "N/A");

        return RiskNarrativeResponse.builder()
                .narrative(narrative)
                .riskFactors(List.of(RiskNarrativeResponse.RiskFactor.builder()
                        .factor("Platform: " + profile.getPlatform().getName())
                        .severity("MEDIUM").description("System default assessment").build()))
                .recommendation("MANUAL_REVIEW").confidenceLevel("LOW").aiGenerated(false).build();
    }

    private MultiplierSuggestionResponse buildFallbackSuggestion(String paramKey) {
        double suggested = 1.0;
        String reasoning = "AI unavailable. Using default baseline multiplier.";
        String key = paramKey.toUpperCase();
        if (key.contains("CRYPTO") || key.contains("POLITICS")) { suggested = 1.5; reasoning = "High-risk category keyword detected."; }
        else if (key.contains("FASHION") || key.contains("FOOD") || key.contains("TRAVEL")) { suggested = 0.9; reasoning = "Lower-risk category keyword detected."; }
        return MultiplierSuggestionResponse.builder().suggestedMultiplier(suggested)
                .reasoning(reasoning).industryBenchmark("Unavailable").confidenceLevel("LOW").aiGenerated(false).build();
    }
}
