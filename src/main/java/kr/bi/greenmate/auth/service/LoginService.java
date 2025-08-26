package kr.bi.greenmate.auth.service;

import kr.bi.greenmate.auth.domain.RefreshToken;
import kr.bi.greenmate.auth.dto.LoginRequest;
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
public class LoginService {

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
        String accessToken = jwtUtil.createJwt(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getNickname(),
                jwtProperties.getAccessTokenValidityInMs());

        // 유효 시간 최대 7일
        String refreshToken = getRefreshToken(user);

        return createTokenResponse(accessToken, refreshToken);
    }

    private TokenResponse createTokenResponse(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
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

        String newRefreshToken = jwtUtil.createJwt(
                String.valueOf(userId),
                user.getEmail(),
                user.getNickname(),
                jwtProperties.getRefreshTokenValidityInMs());

        refreshTokenRepository.save(RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .build());

        return newRefreshToken;
    }
}
