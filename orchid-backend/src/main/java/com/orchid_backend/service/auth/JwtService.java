package com.orchid_backend.service.auth;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.orchid_backend.model.entity.RefreshToken;
import com.orchid_backend.model.entity.Role;
import com.orchid_backend.model.entity.User;
import com.orchid_backend.repository.RefreshTokenRepository;
import com.orchid_backend.repository.UserRepository;
import com.orchid_backend.util.error.ObjectNotFoundException;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class JwtService {

    @Value("${app.jwt.refreshToken.expiration-in-seconds}")
    private Long refreshTokenExpiration;

    @Value("${app.jwt.accessToken.expiration-in-seconds}")
    private Long accessTokenExpiration;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public JwtService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokenRepository, JwtDecoder jwtDecoder,
            UserRepository userRepository) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
    }

    private User fetchUserData(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }

    // === REFRESH TOKEN (dùng DB) ===
    public String createRefreshToken(String username) {
        User user = this.fetchUserData(username);
        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenExpiration);

        RefreshToken refreshToken = new RefreshToken(tokenValue, user, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    // Hủy refresh token (khi logout)
    public void revokeRefreshTokens(String username) {
        refreshTokenRepository.deleteByUser(this.fetchUserData(username));
    }

    // Làm mới access token từ refresh token
    public String refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }

        if (refreshToken.getIsRevoked()) {
            throw new BadCredentialsException("Refresh token revoked");
        }

        // 🔒 Revoke refresh token cũ
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        Role role = refreshToken.getUser().getRole(); // giả sử getRole() trả về Role, không phải Collection<Role>
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.getName());
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(authority);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                refreshToken.getUser().getEmail(),
                "",
                authorities);

        return createAccessToken(userDetails);
    }

    // === ACCESS TOKEN (không dùng DB) ===
    public String createAccessToken(UserDetails userDetails) {
        Instant now = Instant.now();
        String username = userDetails.getUsername();
        String scope = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("your-app")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenExpiration)) // dùng config
                .subject(username)
                .claim("scope", scope)
                .build();

        // ✅ DÙNG JwsHeader CỦA SPRING SECURITY
        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256).build(), // ← truyền "RS256" dưới dạng String
                        claims))
                .getTokenValue();

    }

    // === VALIDATE TOKEN ===
    // ✅ Kiểm tra refresh token hợp lệ (qua DB)
    public boolean isValidRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElse(null);
        if (refreshToken == null) {
            return false;
        }
        if (refreshToken.getIsRevoked()) {
            return false;
        }
        return !refreshToken.getExpiryDate().isBefore(Instant.now());
    }

    // ✅ Lấy email từ refresh token (qua DB)
    public String extractEmailFromRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.getIsRevoked()) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token has expired");
        }

        return refreshToken.getUser().getEmail();
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false) // hoặc dùng config
                .sameSite("Strict")
                .path("/") // ← đảm bảo khớp với lúc tạo
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }

}