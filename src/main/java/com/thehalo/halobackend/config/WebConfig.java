package com.thehalo.halobackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration for serving static files
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // SECURITY: All uploaded files (platform docs, claims, policies) are served
        // exclusively through the JWT-authenticated FileController at /api/v1/files/download/**.
        // Static resource handlers have been intentionally removed to prevent
        // unauthenticated access to sensitive documents via direct URL.
    }
}
