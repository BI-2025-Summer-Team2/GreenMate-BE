package kr.bi.greenmate.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.bi.greenmate.auth.dto.LoginRequest;
import kr.bi.greenmate.auth.dto.ReissueTokenRequest;
import kr.bi.greenmate.auth.dto.TokenResponse;
import kr.bi.greenmate.auth.service.LoginService;
import kr.bi.greenmate.auth.service.ReissueAccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 인가", description = "로그인 API")
@Slf4j
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final LoginService loginService;
    private final ReissueAccessTokenService reissueAccessTokenService;

    @Operation(summary = "로그인", description = "email과 password로 access 토큰과 refresh 토큰을 발행합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = loginService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "access token 재발행", description = "refresh token으로 access token을 새롭게 발행합니다.")
    @PostMapping("/reissue-token")
    public ResponseEntity<TokenResponse> reissueToken(@RequestBody ReissueTokenRequest request) {
        TokenResponse tokenResponse = reissueAccessTokenService.reissueToken(request);
        return ResponseEntity.ok(tokenResponse);
    }
}
