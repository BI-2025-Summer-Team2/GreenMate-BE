package kr.bi.greenmate.green_team_post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "환경 활동 모집글 좋아요 응답 DTO")
public class GreenTeamPostLikeResponse {

  @Schema(description = "좋아요 여부", example = "true")
  private boolean liked;

  @Schema(description = "좋아요 개수", example = "1")
  private long likeCount;

  public static GreenTeamPostLikeResponse of(boolean liked, long likeCount) {
    return GreenTeamPostLikeResponse.builder()
        .liked(liked)
        .likeCount(likeCount)
        .build();
  }
}
