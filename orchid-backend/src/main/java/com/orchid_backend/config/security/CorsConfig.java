package com.orchid_backend.config.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
        private static final List<String> allowedOrigins = Arrays.asList(
                        "http://localhost:5137",
                        "http://localhost:4137");

        private static final List<String> allowedMethods = Arrays.asList(
                        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

        private static final List<String> allowedHeaders = Arrays.asList(
                        "Authorization", // bearer token với jwt
                        "Content-Type",
                        "Accept",
                        "X-Requested-With",
                        "x-no-retry", // customizational request
                        "X-CSRF-TOKEN" // request stick with csrf for session-based auth
        );
        private static final List<String> exposedHeaders = Arrays.asList(
                        "Authorization",
                        "X-Refresh-Token",
                        "Set-Cookie");

        @Bean
        public CorsConfigurationSource configurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(allowedOrigins);
                configuration.setAllowedMethods(allowedMethods);
                configuration.setAllowedHeaders(allowedHeaders);
                configuration.setExposedHeaders(exposedHeaders); // Các header mà mà front được đọc
                configuration.setAllowCredentials(true); // Cho phép request gửi kèm cookie lên server
                configuration.setMaxAge(3600L);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration); // Gán các config cho mọi đường dẫn /**
                return source;
        }

}
