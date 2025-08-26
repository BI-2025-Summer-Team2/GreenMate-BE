package kr.bi.greenmate.auth.service;

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

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보에 일치하는 회원이 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("로그인 정보에 일치하는 회원이 없습니다.");
        }

        // 유효 시간 1시간
        String accessToken = createAccessToken(user);
        // 유효 시간 최대 7일
        String refreshToken = getRefreshToken(user);

        return createTokenResponse(accessToken, refreshToken);
    }

    public TokenResponse reissueToken(ReissueTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        if (jwtUtil.isExpired(refreshTokenValue)) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        User user = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")).getUser();

        String newAccessToken = createAccessToken(user);

        return createTokenResponse(newAccessToken, refreshTokenValue);
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

    private String getRefreshToken(User user) {
        Long userId = user.getId();
        Optional<RefreshToken> existingRefreshToken = refreshTokenRepository.findByUserId(userId);

        if (existingRefreshToken.isPresent()) {
            String refreshTokenValue = existingRefreshToken.get().getToken();

            if (!jwtUtil.isExpired(refreshTokenValue)) {
                return refreshTokenValue;
            }
            refreshTokenRepository.deleteByUserId(userId);
        }
        return createRefreshToken(user);
    }

    private String createRefreshToken(User user) {
        String newRefreshToken = jwtUtil.createRefreshToken(
                String.valueOf(user.getId()),
                jwtProperties.getRefreshTokenValidityInMs());

        refreshTokenRepository.save(RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .build());

        return newRefreshToken;
    }
}
