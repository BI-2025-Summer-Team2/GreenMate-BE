package kr.bi.greenmate.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.bi.greenmate.auth.dto.CustomUserDetails;
import kr.bi.greenmate.common.dto.CursorSliceResponse;
import kr.bi.greenmate.community.dto.CommunityCommentResponse;
import kr.bi.greenmate.community.dto.CommunityPostDetailResponse;
import kr.bi.greenmate.community.dto.CreateCommunityCommentRequest;
import kr.bi.greenmate.community.dto.CreateCommunityPostRequest;
import kr.bi.greenmate.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
@Tag(name = "커뮤니티", description = "커뮤니티 글 관련 API")
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "커뮤니티 글 생성", description = "작성된 글(JSON)과 이미지(파일)를 DB에 저장합니다.")
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> posts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("data") CreateCommunityPostRequest request,
            @RequestPart(value = "images", required = false) @Parameter(description = "이미지 파일") List<MultipartFile> imageFiles) {
        Long userId = userDetails.getId();
        communityService.createPost(userId, request, imageFiles);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "커뮤니티 댓글 조회", description = "커서 기반 페이지네이션으로 게시글에 작성된 댓글들을 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CursorSliceResponse<CommunityCommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") @Positive int size
    ) {
        CursorSliceResponse<CommunityCommentResponse> response = communityService.getCommentList(postId, cursor, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "커뮤니티 댓글 생성", description = "게시글에 댓글(JSON)과 이미지(파일)을 DB에 저장합니다.")
    @PostMapping(value = "/posts/{postId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postComment(
            @PathVariable @Positive(message = "게시글 ID는 양수여야 합니다.") Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("data") CreateCommunityCommentRequest request,
            @RequestPart(value = "image", required = false) @Parameter(description = "이미지 파일") MultipartFile imageFile
    ) {
        Long userId = userDetails.getId();
        communityService.createComment(postId, userId, request, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "게시글 상세 조회", description = "커뮤니티 게시글 ID로 상세 정보를 조회합니다.")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityPostDetailResponse> getCommunityPostDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "게시글 ID", required = true, example = "1") @PathVariable @NotNull @Positive Long postId
    ) {
        return ResponseEntity.ok(communityService.getPostDetail(userDetails.getId(), postId));
    }
}
