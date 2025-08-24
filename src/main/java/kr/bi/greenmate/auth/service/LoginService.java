package kr.bi.greenmate.auth.service;

import kr.bi.greenmate.auth.dto.LoginRequest;
import kr.bi.greenmate.auth.jwt.JWTUtil;
import kr.bi.greenmate.config.properties.JwtProperties;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("로그인 정보에 일치하는 회원이 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("로그인 정보에 일치하는 회원이 없습니다.");
        }

        // 토큰 유효 시간 1시간
        return jwtUtil.createJwt(user.getEmail(), jwtProperties.getAccessTokenValidityInMs());
    }
}
