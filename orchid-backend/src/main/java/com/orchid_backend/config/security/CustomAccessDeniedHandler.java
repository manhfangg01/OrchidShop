package com.orchid_backend.config.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchid_backend.model.dto.restResponse.RestResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        log.warn("Access denied for request: {} from IP: {}, reason: {}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                accessDeniedException.getMessage());

        RestResponse<Object> errorResponse = new RestResponse<>();
        errorResponse.setStatusCode(HttpStatus.FORBIDDEN.value());
        errorResponse.setError("Forbidden");
        errorResponse.setMessage("Bạn không có quyền truy cập tài nguyên này");
        errorResponse.setData(null);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize access denied error response", e);
            // Fallback response
            response.getWriter().write(
                    "{\"error\":\"Forbidden\",\"message\":\"Access denied: insufficient permissions\"}");
        }
    }
}