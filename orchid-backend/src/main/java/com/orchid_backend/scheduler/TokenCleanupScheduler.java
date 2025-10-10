package com.orchid_backend.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.orchid_backend.service.CleanUpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final CleanUpService cleanupService;

    /**
     * Chạy mỗi 10 phút: 0 giây, mỗi 10 phút (0, 10, 20, 30, 40, 50)
     */
    @Scheduled(fixedRate = 600000) // 600_000 ms = 10 phút
    public void scheduledCleanupRevokedRefreshTokens() {
        log.debug("⏰ Bắt đầu dọn dẹp refresh token đã revoke...");
        cleanupService.cleanRevokedRefreshTokens();
    }
}
