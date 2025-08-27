package kr.bi.greenmate.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.bi.greenmate.auth.domain.RefreshToken;
import kr.bi.greenmate.auth.dto.LoginRequest;
import kr.bi.greenmate.auth.dto.ReissueTokenRequest;
import kr.bi.greenmate.auth.dto.TokenResponse;
import kr.bi.greenmate.auth.jwt.JWTUtil;
import kr.bi.greenmate.auth.repository.RefreshTokenRepository;
import kr.bi.greenmate.config.properties.JwtProperties;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보에 일치하는 회원이 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("로그인 정보에 일치하는 회원이 없습니다.");
        }

        // 유효 시간 1시간
        String accessToken = createAccessToken(user);
        // 유효 시간 최대 7일
        String refreshToken = createRefreshToken(user);
        saveRefreshToken(user, refreshToken, servletRequest);

        return createTokenResponse(accessToken, refreshToken);
    }

    public TokenResponse reissueToken(ReissueTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        if (jwtUtil.isExpired(refreshTokenValue)) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        User user = refreshToken.getUser();
        validateRefreshToken(refreshToken, request.getUserId(), user.getId());

        String newAccessToken = createAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        refreshToken.rotate(newRefreshToken, jwtUtil.getExpiredAt(jwtProperties.getRefreshTokenValidityInMs()));

        return createTokenResponse(newAccessToken, newRefreshToken);
    }

    private TokenResponse createTokenResponse(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String createAccessToken(User user) {
        return jwtUtil.createAccessToken(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getNickname(),
                jwtProperties.getAccessTokenValidityInMs());
    }

    private String createRefreshToken(User user) {
        return jwtUtil.createRefreshToken(
                String.valueOf(user.getId()),
                jwtProperties.getRefreshTokenValidityInMs());
    }

    private String getIp(HttpServletRequest servletRequest) {
        String ip = servletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = servletRequest.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = servletRequest.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = servletRequest.getRemoteAddr();
        }
        return ip;
    }

    private void saveRefreshToken(User user, String tokenValue, HttpServletRequest servletRequest) {
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUserAndIssuedIpAndIssuedUserAgent(user, getIp(servletRequest), servletRequest.getHeader("User-Agent"));
        LocalDateTime expiredAt = jwtUtil.getExpiredAt(jwtProperties.getRefreshTokenValidityInMs());

        if (existingRefreshToken.isPresent()) {
            RefreshToken refreshToken = existingRefreshToken.get();
            refreshToken.rotate(tokenValue, expiredAt);
            return;
        }
        refreshTokenRepository.save(RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .issuedUserAgent(servletRequest.getHeader("User-Agent"))
                .issuedIp(getIp(servletRequest))
                .expired_at(expiredAt)
                .build());
    }

    private void validateRefreshToken(RefreshToken refreshToken, Long userId, Long userIdOfToken){
        // 만료 시간 변조 가능성
        // 요청 유저가 토큰 식별자와 일치하는지 확인
        if (refreshToken.isExpired() || !userId.equals(userIdOfToken)) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }
}
