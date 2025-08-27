package kr.bi.greenmate.auth.repository;

import kr.bi.greenmate.auth.domain.RefreshToken;
import kr.bi.greenmate.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserAndIssuedIpAndIssuedUserAgent(User user, String issuedIp, String issuedUserAgent);
}
