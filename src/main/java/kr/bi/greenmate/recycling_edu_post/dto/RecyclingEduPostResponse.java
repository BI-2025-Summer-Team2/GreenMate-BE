package kr.bi.greenmate.recycling_edu_post.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import kr.bi.greenmate.recycling_edu_post.domain.RecyclingEduPost;

@Getter
@Builder
@Schema(description = "분리수거 학습 게시글 응답 DTO")
public class RecyclingEduPostResponse {

  @Schema(description = "게시글 ID", example = "1")
  private final Long id;

  @Schema(description = "게시글 제목", example = "페트병 분리수거 방법")
  private final String title;

  @Schema(description = "게시글 본문 내용", example = "페트병은 라벨을 제거하고 깨끗이 헹군 후 배출해야 합니다.")
  private final String content;

  @Schema(description = "대표 이미지 URL", example = "https://greenmate.kr/images/recycling1.jpg")
  private final String imageUrl;

  @Schema(description = "게시글 생성일시", example = "2025-08-07T10:00:00")
  private final LocalDateTime createdAt;

  public static RecyclingEduPostResponse from(RecyclingEduPost post, String imageUrl) {
    return RecyclingEduPostResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .imageUrl(imageUrl)
        .createdAt(post.getCreatedAt())
        .build();
  }
}
