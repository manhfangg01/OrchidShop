package com.orchid_backend.config.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchid_backend.model.dto.restResponse.RestResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// đối với Jwt -> BearerTokenFilter và UsernamePasswordFilter 
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint { // Nơi xử lý lỗi mà
                                                                                  // ExceptionTranslation đưa ra cụ thể
                                                                                  // là 401 unauthenticated
    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper; // dùng gói các lỗi thành chuổi JSOn

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        log.warn("Unauthenticated access attempt: {}", authException.getMessage());

        RestResponse<Object> errorResponse = new RestResponse<>();
        errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setError("Unauthorized");
        errorResponse.setMessage("Bạn chưa đăng nhập hoặc token không hợp lệ");
        errorResponse.setData(null);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse); // chuyển thành JSON gửi cho client
            response.getWriter().write(jsonResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to write error response", e);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
        }
    }
}
