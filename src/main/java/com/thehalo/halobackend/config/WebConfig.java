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
        // Serve uploaded files from the uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        
        // Serve platform documents
        registry.addResourceHandler("/platforms/**")
                .addResourceLocations("file:uploads/platforms/");
        
        // Serve claim documents
        registry.addResourceHandler("/claims/**")
                .addResourceLocations("file:uploads/claims/");
        
        // Serve policy documents
        registry.addResourceHandler("/policies/**")
                .addResourceLocations("file:uploads/policies/");
    }
}
