package com.orchid_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.orchid_backend.model.entity.RefreshToken;
import com.orchid_backend.model.entity.User;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    /**
     * Xóa tất cả refresh token đã bị revoke (isRevoked = true)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.isRevoked = true")
    void deleteAllRevokedRefreshTokens();
}
