package kr.bi.greenmate.green_team_post.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import kr.bi.greenmate.common.dto.IdResponse;
import kr.bi.greenmate.green_team_post.dto.GreenTeamPostCreateRequest;
import kr.bi.greenmate.green_team_post.service.GreenTeamPostCommandService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/green-team-posts")
@Tag(name = "환경 활동 모집글 API", description = "환경 활동 모집글 생성 API")
public class GreenTeamPostController {

  private final GreenTeamPostCommandService service;

  @Operation(summary = "모집글 생성", description = "환경 활동 모집글을 생성합니다.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<IdResponse> create(
      @ModelAttribute @Valid GreenTeamPostCreateRequest request,
      @AuthenticationPrincipal(expression = "id") Long userId
  ) {
    Long id = service.create(userId, request);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(id)
        .toUri();

    return ResponseEntity
        .created(location)
        .body(new IdResponse(id));
  }
}
