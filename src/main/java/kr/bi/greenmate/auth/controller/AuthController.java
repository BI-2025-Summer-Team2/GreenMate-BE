package kr.bi.greenmate.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.bi.greenmate.auth.dto.LoginRequest;
import kr.bi.greenmate.auth.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "인증 인가", description = "로그인 API")
@Slf4j
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {
    private final LoginService loginService;

    @Operation(summary = "로그인", description = "email과 password로 access 토큰을 발행합니다.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = loginService.login(request);
        Map<String, String> response = Map.of("message", "로그인에 성공했습니다.");
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).body(response);
    }
}
