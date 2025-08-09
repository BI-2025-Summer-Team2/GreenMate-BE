package kr.bi.greenmate.recycling_edu_post.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import kr.bi.greenmate.recycling_edu_post.dto.RecyclingEduPostResponse;
import kr.bi.greenmate.recycling_edu_post.service.RecyclingEduPostService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/recycling-edu-posts")
@Tag(name = "분리수거 학습 게시글 API", description = "분리수거 학습 게시글 조회 API")
public class RecyclingEduPostController {

  private final RecyclingEduPostService service;

  @Operation(summary = "전체 목록 조회", description = "분리수거 학습 게시글 목록을 조회합니다.")
  @GetMapping
  public List<RecyclingEduPostResponse> getAllPosts() {
    return service.getAllPosts();
  }

  @Operation(summary = "단일 게시글 조회", description = "특정 분리수거 학습 게시글을 조회합니다.")
  @GetMapping("/{id}")
  public RecyclingEduPostResponse getPostById(
      @PathVariable @NotNull @Min(1) Long id
  ) {
    return service.getPostById(id);
  }
}
