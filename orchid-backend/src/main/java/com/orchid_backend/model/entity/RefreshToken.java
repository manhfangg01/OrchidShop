package com.orchid_backend.model.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // Sẽ lưu dưới dạng UUID / Opaque token hay token mù

    @ManyToOne(fetch = FetchType.LAZY) // đây là quan hệ 1 phía bửa ta không có nhu cầu truy vấn token từ user
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private Boolean isRevoked; // khi nào token tạo thì cái này cũng sẽ là false, tức là chưa
                               // bị
                               // thu hồi bị hủy

    public RefreshToken(String token, User user, Instant expiryDate) {
        this.isRevoked = Boolean.FALSE;
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }
}
