package kr.bi.greenmate.green_team_post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "환경 활동 모집글 요약 응답 DTO")
public class GreenTeamPostSummaryResponse {

  @Schema(description = "모집글 ID", example = "1")
  private Long id;

  @Schema(description = "작성자 ID", example = "1")
  private Long userId;

  @Schema(description = "작성자 닉네임", example = "nickname")
  private String nickname;

  @Schema(description = "모집글 제목", example = "한강 플로깅 함께해요")
  private String title;

  @Schema(description = "모집글 요약 내용 (본문 최대 20자 + ... 표시)", example = "함께 한강을 걸으며 쓰레기를 주워요!...")
  private String content;

  @Schema(description = "현재 참여자 수", example = "5")
  private Integer participantCount;

  @Schema(description = "모집 정원 (최대 참여자 수)", example = "20")
  private Integer maxParticipants;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "활동일", example = "2025-09-01T10:00:00")
  private LocalDateTime eventDate;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "신청 마감일", example = "2025-08-30T23:59:59")
  private LocalDateTime deadlineAt;
}
