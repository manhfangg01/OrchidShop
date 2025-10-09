package com.orchid_backend.service.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.orchid_backend.model.entity.RefreshToken;
import com.orchid_backend.model.entity.User;
import com.orchid_backend.repository.RefreshTokenRepository;

@Service
public class JwtService {

    @Value("${app.jwt.refreshToken.expiration-in-seconds}")
    private Long refreshTokenExpiration;

    @Value("${app.jwt.accessToken.expiration-in-seconds}")
    private Long accessTokenExpiration;

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // === REFRESH TOKEN (dùng DB) ===
    public String createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenExpiration);

        RefreshToken refreshToken = new RefreshToken(tokenValue, user, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }

    // Hủy refresh token (khi logout)
    public void revokeRefreshTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
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

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(refreshToken.getUser().getEmail())
                .password("")
                .authorities("ROLE_USER")
                .build();

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
}