package kr.bi.greenmate.green_team_post.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import kr.bi.greenmate.common.dto.IdResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostCreateRequest;
import kr.bi.greenmate.green_team_post.service.GreenTeamPostCommandService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/green-team-posts")
@Tag(name = "환경 활동 모집글 API", description = "환경 활동 모집글 생성 API")
public class GreenTeamPostController {

  private final GreenTeamPostCommandService service;

  @Operation(
      summary = "환경 활동 모집글 생성",
      description = "JSON 데이터는 `data` 필드(application/json)에, 이미지는 `images` 필드에 담아 전송합니다."
  )
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<IdResponse> create(
      @Parameter(description = "환경 활동 모집글 생성 생성 JSON(data)", required = true)
      @Valid
      @RequestPart("data") GreenTeamPostCreateRequest data,

      @Parameter(description = "첨부 이미지(images, 0~3장)", required = false)
      @RequestPart(value = "images", required = false) List<MultipartFile> images,

      @Parameter(hidden = true, description = "인증된 사용자 ID (자동 주입)")
      @AuthenticationPrincipal(expression = "id") Long userId
  ) {
    List<MultipartFile> safeImages = (images == null) ? List.of() : images;

    Long id = service.create(userId, data, safeImages);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();

    return ResponseEntity.created(location).body(new IdResponse(id));
  }
}
