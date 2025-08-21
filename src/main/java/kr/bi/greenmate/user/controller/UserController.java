package kr.bi.greenmate.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.bi.greenmate.common.annotation.SwaggerBody;
import kr.bi.greenmate.user.dto.SignUpRequest;
import kr.bi.greenmate.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원 관리", description = "회원 관련 API")
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @Operation(summary = "회원가입", description = "사용자의 정보(JSON)와 프로필 이미지(파일)를 받아 회원으로 등록합니다.")
    @SwaggerBody(content = @Content(encoding = @Encoding(name = "userInfo", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> signUp(@RequestPart("userInfo") @Valid SignUpRequest request,
                                       @RequestPart(value = "profileImage", required = false) @Parameter(description = "프로필 이미지 파일") MultipartFile profileImage) {

        userService.signUp(request, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
