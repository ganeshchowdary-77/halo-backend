package com.thehalo.halobackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Binds all {@code app.ai.*} properties from {@code application.properties}.
 * <p>
 * This class is intentionally a pure binding class — it holds NO hardcoded defaults.
 * All values (model, baseUrl, temperature, maxTokens) must be explicitly configured
 * in {@code application.properties} (or overridden via environment variables).
 * This makes the single source of truth for AI configuration explicit and visible.
 *
 * <pre>
 *   app.ai.api-key      = ${GROQ_API_KEY}
 *   app.ai.model        = llama3-70b-8192
 *   app.ai.base-url     = https://api.groq.com/openai/v1
 *   app.ai.temperature  = 0.2
 *   app.ai.max-tokens   = 2048
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "app.ai")
@Getter
@Setter
public class AiProviderConfig {

    private String apiKey;
    private String model;
    private String baseUrl;
    private double temperature;
    private int    maxTokens;

    @Bean(name = "aiRestTemplate")
    public RestTemplate aiRestTemplate() {
        return new RestTemplate();
    }

    /** Returns true if the Groq API key is set and non-blank. */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
