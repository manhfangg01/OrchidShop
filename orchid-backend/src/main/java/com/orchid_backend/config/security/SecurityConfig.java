package com.orchid_backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private CorsConfig corsConfig;
    private final ObjectMapper objectMapper;

    public SecurityConfig(CorsConfig corsConfig, ObjectMapper objectMapper) {
        this.corsConfig = corsConfig;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles"); // hoặc "realm_access.roles" nếu dùng Keycloak

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] whiteList = {
                "/", "/api/auth/login", "/api/auth/refresh", "/api/auth/signup",
                "/api/auth/request-reset-link",
                "/api/auth/reset-password",
                "/api/auth/me",
                "/storage/**",

        };

        http.csrf(csrf -> csrf.disable()); // Nếu dùng session-based Authentication thì bật, do session có thể gửi lên
                                           // sessionid kèm csrf còn restful api thì không gửi được
        http.cors(cors -> cors.configurationSource(corsConfig.configurationSource()));
        http.formLogin(form -> form.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(whiteList).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper))
                .accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper)));
        http.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))); // Dùng Jwt
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // Server không tạo và dùng http session để đăng nhập và lưu thông tin người
        // dùng nữa
        return http.build();
    }

    // Client → [SecurityFilterChain]
    // ↓
    // BearerTokenAuthenticationFilter → (thất bại) → ném AuthenticationException
    // (401)
    // ↓
    // ExceptionTranslationFilter → bắt exception
    // ↓
    // Gọi AuthenticationEntryPoint.commence()
    // ↓
    // Trả response 401 (JSON, redirect, v.v.)
    // ↓
    // ✅ Request kết thúc — KHÔNG vào controller
}
