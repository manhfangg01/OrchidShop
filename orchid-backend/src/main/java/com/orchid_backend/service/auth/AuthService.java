package com.orchid_backend.service.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.orchid_backend.model.dto.auth.LoginRequest;
import com.orchid_backend.model.dto.auth.LoginResponse;
import com.orchid_backend.model.dto.auth.SignUpRequest;
import com.orchid_backend.model.entity.User;
import com.orchid_backend.repository.UserRepository;
import com.orchid_backend.util.error.DuplicatedObjectException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refreshToken.expiration-in-seconds}")
    private Long refreshTokenExpiration;

    @Value("${app.jwt.accessToken.expiration-in-seconds}")
    private Long accessTokenExpiration;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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
                .build();

        userRepository.save(newUser);
    }

    public String createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return jwtService.createRefreshToken(user);
    }

    public LoginResponse login(LoginRequest request) {
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
        String refreshToken = this.createRefreshToken(userDetails.getUsername());

        // 6. Tạo access token
        String accessToken = jwtService.createAccessToken(userDetails);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenValidity(accessTokenExpiration)
                .refreshTokenValidity(refreshTokenExpiration)
                .build();
    }

}
