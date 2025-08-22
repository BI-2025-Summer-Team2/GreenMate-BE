package kr.bi.greenmate.green_team_post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import kr.bi.greenmate.green_team_post.domain.LocationType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "환경 활동 모집글 생성 요청 DTO")
public class GreenTeamPostCreateRequest {

  @NotBlank
  @Size(max = 50)
  @Schema(description = "모집글 제목", example = "한강 플로깅 함께해요", maxLength = 50)
  private String title;

  @NotBlank
  @Size(max = 4000)
  @Schema(description = "모집글 본문 내용", example = "함께 한강을 걸으며 쓰레기를 주워요!", maxLength = 4000)
  private String content;

  @NotNull
  @Schema(description = "활동 위치 유형", example = "CIRCLE", allowableValues = {"CIRCLE", "POLYGON"})
  private LocationType locationType;

  @NotBlank
  @Schema(description = "GeoJSON 문자열 (활동 위치 좌표)",
  private String locationGeojson;
      example = "{\"center\":{\"lat\":36.61029738127031,\"lng\":127.28513401351398},\"radius\":392.24111263876654}")

  @NotNull
  @Min(1)
  @Schema(description = "모집 정원 (최대 참여자 수)", example = "20", minimum = "1")
  private Integer maxParticipants;

  @NotNull
  @Future(message = "활동일은 현재 시점 이후로 설정해주세요")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "활동일", example = "2025-09-01T10:00:00")
  private LocalDateTime eventDate;

  @NotNull
  @Future(message = "신청 마감일은 현재 시점 이후로 설정해주세요")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "신청 마감일", example = "2025-08-30T23:59:59")
  private LocalDateTime deadlineAt;

  @AssertTrue(message = "신청 마감일은 활동일과 같거나 그 이전이어야 합니다")
  private boolean isDeadlineBeforeEvent() {
    if (deadlineAt == null || eventDate == null) {
      return true;
    }
    return !deadlineAt.isAfter(eventDate);
  }
}
