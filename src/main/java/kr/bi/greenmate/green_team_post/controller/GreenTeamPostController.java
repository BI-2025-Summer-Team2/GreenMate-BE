package kr.bi.greenmate.green_team_post.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.bi.greenmate.common.dto.IdResponse;
import kr.bi.greenmate.common.pagination.dto.PageResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostCreateRequest;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostSummaryResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostDetailResponse;
import kr.bi.greenmate.green_team_post.service.GreenTeamPostCommandService;
import kr.bi.greenmate.green_team_post.service.GreenTeamPostQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/green-team-posts")
@Tag(name = "환경 활동 모집글 API", description = "환경 활동 모집글 생성 API")
public class GreenTeamPostController {

  private final GreenTeamPostCommandService commandService;
  private final GreenTeamPostQueryService queryService;

  @Operation(
      summary = "환경 활동 모집글 생성",
      description = "멀티파트 요청: `data`(application/json) + `images`(파일 배열, 0~3장)",
      requestBody = @RequestBody(
          content = @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              encoding = {
                  @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                  @Encoding(name = "images", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
              }
          )
      )
  )
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<IdResponse> createGreenTeamPost(
      @Valid
      @RequestPart("data") GreenTeamPostCreateRequest data,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
  ) {
    List<MultipartFile> safeImages = (images == null) ? List.of() : images;

    Long userId = 1L; // TODO: 유저 인증 기능 구현 후 삭제
    Long id = commandService.create(userId, data, safeImages);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();

    return ResponseEntity.created(location).body(new IdResponse(id));
  }

  @Operation(summary = "환경 활동 모집글 목록 조회", description = "최신 등록순으로 환경 활동 모집글 전체 목록을 반환합니다.")
  @GetMapping
  public ResponseEntity<PageResponse<GreenTeamPostSummaryResponse>> getGreenTeamPostList(
      @Parameter(description = "페이지 탐색을 위한 커서 값(Base64 인코딩). 첫 페이지 요청 시 생략 가능", required = false)
      @RequestParam(value = "cursor", required = false) String cursor,
      @Parameter(description = "페이지 크기(기본 20, 최대 50)", required = false, example = "20")
      @RequestParam(value = "size", required = false) Integer size
  ) {
    return ResponseEntity.ok(queryService.getPostList(size, cursor));
  }

  @Operation(summary = "환경 활동 단일 모집글 조회", description = "특정 ID의 환경 활동 모집글을 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<GreenTeamPostDetailResponse> getGreenTeamPostById(
      @Parameter(
          description = "조회할 환경 활동 모집글 ID (1 이상의 정수)",
          required = true,
          example = "1"
      )
      @PathVariable @NotNull @Min(1) Long id
  ) {
    return ResponseEntity.ok(queryService.getPostDetail(id));
  }
}
