package kr.bi.greenmate.auth.service;

import kr.bi.greenmate.auth.dto.ReissueTokenRequest;
import kr.bi.greenmate.auth.dto.TokenResponse;
import kr.bi.greenmate.auth.jwt.JWTUtil;
import kr.bi.greenmate.auth.repository.RefreshTokenRepository;
import kr.bi.greenmate.config.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReissueAccessTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public TokenResponse reissueToken(ReissueTokenRequest request) {

        String refreshTokenValue = request.getRefreshToken();

        if (jwtUtil.isExpired(refreshTokenValue)) {
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        String newAccessToken = jwtUtil.createJwt(
                jwtUtil.getClaim(refreshTokenValue, "user_id"),
                jwtUtil.getClaim(refreshTokenValue, "user_email"),
                jwtUtil.getClaim(refreshTokenValue, "user_nickname"),
                jwtProperties.getAccessTokenValidityInMs());

        return TokenResponse.builder()
                .refreshToken(refreshTokenValue)
                .accessToken(newAccessToken)
                .build();
    }
}
