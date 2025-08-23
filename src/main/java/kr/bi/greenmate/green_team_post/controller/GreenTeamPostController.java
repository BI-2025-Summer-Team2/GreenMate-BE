package kr.bi.greenmate.green_team_post.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<IdResponse> create(
      @Valid
      @RequestPart("data") GreenTeamPostCreateRequest data,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
  ) {
    List<MultipartFile> safeImages = (images == null) ? List.of() : images;

    Long userId = 1L; // 유저 인증 기능 구현 후 삭제
    Long id = service.create(userId, data, safeImages);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();

    return ResponseEntity.created(location).body(new IdResponse(id));
  }
}
