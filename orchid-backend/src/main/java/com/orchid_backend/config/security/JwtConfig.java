package com.orchid_backend.config.security;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {
    public static final SignatureAlgorithm JWT_ALGORITHM = SignatureAlgorithm.RS256; // nên dùng RS256 vì các hệ thống
                                                                                     // lớn thường hay xài
    @Value("${app.jwt.private}")
    private String jwtPrivate;

    @Value("${app.jwt.public}")
    private String jwtPublic;

    private RSAKey getRsaKey() {
        try {
            // Xử lý private key
            String privateKeyPEM = jwtPrivate
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\s", "");
            // Giải mã Base64 và tạo RSAPrivateKey
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM); // chuyển private key thành byte thô
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);// PKCS#8 (chuẩn phổ biến cho
                                                                                          // private key).
            KeyFactory kf = KeyFactory.getInstance("RSA"); // tạo keyFactory nơi sinh Key
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) kf.generatePrivate(privateKeySpec); // ép kiểu từ Private key
                                                                                              // định dạng PKCS#8 sang
                                                                                              // RSAPrivateKey

            // Xử lý public key (nếu có)
            String publicKeyPEM = jwtPublic
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\s", "");

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);// byte thô public key
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes); // public key chuẩn X509
            RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(publicKeySpec); // sinh key và ép kiểu thành
                                                                                         // Rsa Public key
            // Tạo RSAKey với cả private và public
            return new RSAKey.Builder(rsaPublicKey)
                    .privateKey(rsaPrivateKey)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA keys", e);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        RSAKey rsaKey = getRsaKey();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            RSAKey rsaKey = getRsaKey();
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to create RSA public key for JWT decoder" + e.getMessage());
        }
    }

    // trong runtime, JOSEException có thể bị bắt lại khi
    // ❌ Chữ ký không hợp lệ - Token bị sửa đổi, hoặc ký bằng private key khác với
    // public key đang dùng để verify - BadJwtException
    // ❌ Token hết hạn - Claim exp (expiration time) đã qua - ExpiredJwtException
    // ❌ Định dạng JWT sai - Không phải JWT hợp lệ (thiếu 3 phần, base64 invalid,
    // v.v.) - MalformedJwtException
    // ❌ Không có chữ ký (unsigned JWT) - Token không được ký (plain JWT) nhưng bạn
    // yêu cầu phải có chữ ký - BadJwtException

}
