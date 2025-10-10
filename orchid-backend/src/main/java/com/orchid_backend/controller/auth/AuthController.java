package com.orchid_backend.controller.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orchid_backend.model.dto.auth.AccountResponse;
import com.orchid_backend.model.dto.auth.LoginRequest;
import com.orchid_backend.model.dto.auth.LoginResponse;
import com.orchid_backend.model.dto.auth.SignUpRequest;
import com.orchid_backend.model.dto.restResponse.ApiMessage;
import com.orchid_backend.service.auth.AuthService;
import com.orchid_backend.service.auth.JwtService;
import com.orchid_backend.service.auth.AuthService.AuthResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;

    }

    @Value("${app.jwt.refreshToken.expiration-in-seconds}")
    private Long refreshTokenExpiration;

    @Value("${app.jwt.accessToken.expiration-in-seconds}")
    private Long accessTokenExpiration;

    @PostMapping("/login")
    @ApiMessage("Login with username and password")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // Gọi service để xử lý logic xác thực và tạo token
        AuthResult authResult = authService.login(request);

        // Tạo cookie refresh token
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResult.getRefreshToken())
                .httpOnly(true)
                .secure(false) // Chỉ có HTTPS mới được gửi kèm cookie có thuộc tính "Secure"
                .sameSite("Strict")
                .path("/")
                .maxAge(authResult.getRefreshTokenValidity())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponse(authResult.getAccessToken(), authResult.getAccessTokenValidity()));
    }

    @PostMapping("/signup")
    @ApiMessage("Sign up for new account")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        // Trả về 201 Created + không có body
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/account")
    public ResponseEntity<AccountResponse> getAccount(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        return ResponseEntity.ok(authService.account(email));
    }

    @PostMapping("/refresh")
    @ApiMessage("refresh access token")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Lấy refresh token từ cookie
        String refreshToken = this.authService.getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate refresh token
        if (!jwtService.isValidRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Lấy email từ refresh token
        String username = jwtService.extractEmailFromRefreshToken(refreshToken);

        // từ username có được trong refreshToken loai userDetails
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Tạo lại access token
        String accessToken = jwtService.createAccessToken(userDetails);
        String newRefreshToken = jwtService.createRefreshToken(username);

        // Gửi lại refresh token mới qua cookie
        ResponseCookie newRefreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(false) // dev: false; production: true
                .sameSite("Strict")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshCookie.toString())
                .body(new LoginResponse(accessToken, accessTokenExpiration));
    }

    @PostMapping("/logout")
    @ApiMessage("Log out an account")
    public ResponseEntity<Void> logOut(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = this.authService.getRefreshTokenFromCookie(request);

        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                String username = jwtService.extractEmailFromRefreshToken(refreshToken);
                if (username != null && !username.isBlank()) {
                    this.jwtService.revokeRefreshTokens(username);
                }
            } catch (Exception e) {
                // Log nếu cần, nhưng không trả lỗi ra client
                logger.warn("Invalid refresh token during logout", e);
            }
        }

        this.jwtService.clearRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

}
