package kr.bi.greenmate.green_team_post.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import kr.bi.greenmate.green_team_post.domain.LocationType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 환경 활동 모집글 생성 요청 DTO
 * - multipart/form-data 바인딩(@ModelAttribute) 기준
 */
@Getter
@Setter
@NoArgsConstructor
public class GreenTeamPostCreateRequest {

  @NotBlank
  @Size(max = 50)
  private String title;

  @NotBlank
  @Size(max = 4000)
  private String content;

  @NotNull
  private LocationType locationType;

  @NotBlank
  private String locationGeojson; // 프론트에서 전달받은 GeoJSON 그대로 저장

  @NotNull
  @Min(1)
  private Integer maxParticipants;

  @NotNull
  @Future(message = "활동일은 현재 시점 이후로 설정해주세요")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime eventDate;

  @NotNull
  @Future(message = "신청 마감일은 현재 시점 이후로 설정해주세요")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime deadlineAt;

  @Size(max = 3)
  private List<MultipartFile> images;

  @AssertTrue(message = "신청 마감일은 활동일과 같거나 그 이전이어야 합니다")
  private boolean isDeadlineBeforeEvent() {
    if (deadlineAt == null || eventDate == null) {
      return true;
    }
    return deadlineAt.isBefore(eventDate) || deadlineAt.isEqual(eventDate);
  }
}
