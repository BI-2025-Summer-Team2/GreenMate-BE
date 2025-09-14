package kr.bi.greenmate.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.bi.greenmate.auth.dto.CustomUserDetails;
import kr.bi.greenmate.common.dto.CursorSliceResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostSummaryResponse;
import kr.bi.greenmate.green_team_post.service.GreenTeamPostQueryService;
import kr.bi.greenmate.user.dto.SignUpRequest;
import kr.bi.greenmate.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원 관리", description = "회원 관련 API")
@Slf4j
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final GreenTeamPostQueryService greenTeamPostQueryService;

    @Operation(summary = "회원가입", description = "사용자의 정보(JSON)와 프로필 이미지(파일)를 받아 회원으로 등록합니다.")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> signUp(@RequestPart("userInfo") @Valid SignUpRequest request,
                                       @RequestPart(value = "profileImage", required = false) @Parameter(description = "프로필 이미지 파일") MultipartFile profileImage) {

        userService.signUp(request, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임의 중복 여부를 확인합니다.")
    @RequestMapping(value = "/nicknames/{nickname}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkNicknameExistence(@PathVariable String nickname){
        userService.validateNicknameIsUnique(nickname);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "참여한 환경 활동 모집글 목록 조회", description = "로그인한 사용자가 참여한 환경 활동 모집글 목록을 반환합니다.")
    @GetMapping("/me/green-team-posts/participated")
    public ResponseEntity<CursorSliceResponse<GreenTeamPostSummaryResponse>> listParticipatedPosts(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(required = false) Long cursorId,
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "size는 1 이상이어야 합니다.")
        @Max(value = 100, message = "size는 100 이하여야 합니다.")
        int size
    ) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(greenTeamPostQueryService.getParticipatedPostList(userId, cursorId, size));
    }
}
