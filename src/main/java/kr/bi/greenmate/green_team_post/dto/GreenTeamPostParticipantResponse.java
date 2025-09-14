package kr.bi.greenmate.green_team_post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "환경 활동 모집글 참가 응답 DTO")
public class GreenTeamPostParticipantResponse {

  @Schema(description = "참가 여부", example = "true")
  private boolean participated;

  @Schema(description = "현재 참가자 수", example = "2")
  private long participantCount;

  public static GreenTeamPostParticipantResponse from(boolean participated, long participantCount) {
    return GreenTeamPostParticipantResponse.builder()
        .participated(participated)
        .participantCount(participantCount)
        .build();
  }
}
