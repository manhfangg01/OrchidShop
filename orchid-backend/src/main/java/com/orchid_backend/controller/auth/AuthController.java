package com.orchid_backend.controller.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orchid_backend.model.dto.auth.LoginRequest;
import com.orchid_backend.model.dto.auth.LoginResponse;
import com.orchid_backend.model.dto.auth.SignUpRequest;
import com.orchid_backend.model.dto.restResponse.ApiMessage;
import com.orchid_backend.service.auth.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ApiMessage("Login with username and password")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // Gọi service để xử lý logic xác thực và tạo token
        LoginResponse authResult = authService.login(request);

        // Tạo cookie refresh token
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResult.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(authResult.getRefreshTokenValidity())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(authResult);
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        // Trả về 201 Created + không có body
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
