package com.thehalo.halobackend.security.config;

import com.thehalo.halobackend.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final Environment environment;

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (JWT based stateless API)
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/api/v1/auth/**").permitAll();
                        
                        // Public Endpoints
                        auth.requestMatchers("/api/v1/public/**").permitAll();
                        auth.requestMatchers("/api/v1/products/public/**").permitAll();
                        auth.requestMatchers("/api/v1/ai/chat/public").permitAll();
                        
                        // File uploads - removed public access, now handled securely via /api/v1/files/download/**

                        // OpenAPI / Swagger — always public
                        auth.requestMatchers("/v3/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();

                        // H2 Console — only expose in dev profile
                        boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev")
                                || Arrays.asList(environment.getDefaultProfiles()).contains("default");
                        if (isDev) {
                            auth.requestMatchers("/h2-console/**").permitAll();
                        }

                        auth.anyRequest().authenticated();
                })

                // Security headers
                .headers(headers -> headers
                        // Allow H2 console iframes only from same origin (dev)
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentTypeOptions(ct -> {})
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )

                // Add JWT filter
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}