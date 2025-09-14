package kr.bi.greenmate.green_team_post.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.bi.greenmate.common.dto.CursorSliceResponse;
import kr.bi.greenmate.common.dto.IdResponse;
import kr.bi.greenmate.auth.dto.CustomUserDetails;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostCreateRequest;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostDetailResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostSummaryResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostLikeResponse;
import kr.bi.greenmate.green_team_post.service.GreenTeamPostCommandService;
import kr.bi.greenmate.green_team_post.service.GreenTeamPostQueryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/green-team-posts")
@Tag(name = "환경 활동 모집글 API", description = "환경 활동 모집글 API")
public class GreenTeamPostController {

  private final GreenTeamPostCommandService commandService;
  private final GreenTeamPostQueryService queryService;

  @Operation(summary = "환경 활동 모집글 생성", description = "모집글(JSON)과 이미지(파일, 0~3장)를 DB에 저장합니다.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<IdResponse> createGreenTeamPost(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestPart("data") GreenTeamPostCreateRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles
  ) {
    Long userId = userDetails.getId();
    Long id = commandService.create(userId, request, imageFiles);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();

    return ResponseEntity.created(location).body(new IdResponse(id));
  }

  @Operation(summary = "환경 활동 모집글 목록 조회", description = "최신 등록순으로 환경 활동 모집글 전체 목록을 반환합니다.")
  @GetMapping
  public ResponseEntity<CursorSliceResponse<GreenTeamPostSummaryResponse>> list(
      @RequestParam(required = false) Long cursorId,
      @RequestParam(defaultValue = "20")
      @Min(value = 1, message = "size는 1 이상이어야 합니다.")
      @Max(value = 100, message = "size는 100 이하여야 합니다.")
      int size
  ) {
    return ResponseEntity.ok(queryService.getPostList(cursorId, size));
  }

  @Operation(summary = "환경 활동 단일 모집글 조회", description = "특정 ID의 환경 활동 모집글을 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<GreenTeamPostDetailResponse> getGreenTeamPostById(
      @Parameter(description = "모집글 ID (1 이상)", required = true, example = "1")
      @PathVariable @NotNull @Min(1) Long id
  ) {
    return ResponseEntity.ok(queryService.getPostDetail(id));
  }

  @Operation(summary = "참여한 환경 활동 모집글 목록 조회", description = "특정 유저가 참여한 환경 활동 모집글 목록을 반환합니다.")
  @GetMapping("/participants/{userId}")
  public ResponseEntity<CursorSliceResponse<GreenTeamPostSummaryResponse>> listParticipatedPosts(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false) Long cursorId,
      @RequestParam(defaultValue = "20")
      @Min(value = 1, message = "size는 1 이상이어야 합니다.")
      @Max(value = 100, message = "size는 100 이하여야 합니다.")
      int size
  ) {
    Long userId = userDetails.getId();

    return ResponseEntity.ok(queryService.getParticipatedPostList(userId, cursorId, size));
  }

  @Operation(summary = "모집글 좋아요 생성", description = "해당 모집글에 좋아요를 생성합니다.")
  @PostMapping("/{postId}/likes")
  public ResponseEntity<GreenTeamPostLikeResponse> addLike(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("postId") Long postId
  ) {
    return ResponseEntity.ok(commandService.addLike(postId, userDetails.getId()));
  }

  @Operation(summary = "모집글 좋아요 삭제", description = "해당 모집글에 좋아요를 삭제합니다.")
  @DeleteMapping("/{postId}/likes")
  public ResponseEntity<GreenTeamPostLikeResponse> removeLike(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable("postId") Long postId
  ) {
    return ResponseEntity.ok(commandService.removeLike(postId, userDetails.getId()));
  }
}
