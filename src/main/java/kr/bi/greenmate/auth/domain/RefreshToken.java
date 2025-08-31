package kr.bi.greenmate.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.bi.greenmate.user.domain.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(length = 255, nullable = false)
    private String token;

    @Column(length = 50)
    private String issuedIp;

    @Column(length = 255)
    private String issuedUserAgent;

    @Column(nullable = false)
    private LocalDateTime expired_at;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expired_at);
    }

    public void rotate(String newToken, LocalDateTime newExpiredAt) {
        this.token = newToken;
        this.expired_at = newExpiredAt;
    }
}
