package com.orchid_backend.service;

import org.springframework.stereotype.Service;

import com.orchid_backend.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanUpService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void cleanRevokedRefreshTokens() {
        try {
            long before = refreshTokenRepository.count(); // (tùy chọn) đếm trước
            refreshTokenRepository.deleteAllRevokedRefreshTokens();
            long after = refreshTokenRepository.count(); // (tùy chọn) đếm sau

            log.info("🧹 Đã dọn dẹp refresh token: {} token đã bị xóa", before - after);
        } catch (Exception e) {
            log.error("❌ Lỗi khi dọn dọn refresh token đã revoke", e);
        }
    }
}
