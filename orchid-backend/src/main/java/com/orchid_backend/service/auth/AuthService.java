package com.orchid_backend.service.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.orchid_backend.model.dto.auth.AccountResponse;
import com.orchid_backend.model.dto.auth.LoginRequest;
import com.orchid_backend.model.dto.auth.SignUpRequest;
import com.orchid_backend.model.entity.User;
import com.orchid_backend.repository.RoleRepository;
import com.orchid_backend.repository.UserRepository;
import com.orchid_backend.util.error.DuplicatedObjectException;
import com.orchid_backend.util.error.ObjectNotFoundException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.accessToken.expiration-in-seconds}")
    private Long accessTokenExpiration;
    @Value("${app.jwt.refreshToken.expiration-in-seconds}")
    private Long refreshTokenExpiration;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
    }

    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getUsername())) {
            throw new DuplicatedObjectException("Email already exists");
        }
        if (userRepository.existsByFullName(request.getFullName())) {
            throw new DuplicatedObjectException("Your account name already exists");
        }

        User newUser = User.builder()
                .email(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(Instant.now())
                .role(roleRepository.findByName("USER")
                        .orElseThrow(() -> new ObjectNotFoundException("Role is invalid")))
                .build();

        userRepository.save(newUser);
    }

    public AuthResult login(LoginRequest request) {
        // 1. Tạo token xác thực
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(request.getUsername(),
                request.getPassword());

        // 2. Xác thực
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 3. Lưu vào SecurityContext (tùy chọn, nhưng tốt cho consistency)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. Lấy thông tin user đã xác thực
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 5. Tạo refresh token
        String refreshToken = jwtService.createRefreshToken(userDetails.getUsername());
        // 6. Tạo access token
        String accessToken = jwtService.createAccessToken(userDetails);

        return AuthResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenValidity(accessTokenExpiration)
                .refreshTokenValidity(refreshTokenExpiration)
                .build();
    }

    public AccountResponse account(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        return AccountResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .id(user.getId())
                .role(user.getRole().getName())
                .build();
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResult {
        private String accessToken;
        private Long accessTokenValidity;
        private String refreshToken;
        private Long refreshTokenValidity;
    }

}
