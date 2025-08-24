package kr.bi.greenmate.green_team_post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import kr.bi.greenmate.green_team_post.domain.LocationType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "환경 활동 모집글 상세 응답 DTO")
public class GreenTeamPostDetailResponse {

  @Schema(description = "모집글 ID", example = "1")
  private Long id;

  @Schema(description = "작성자 ID", example = "1")
  private Long userId;

  @Schema(description = "작성자 닉네임", example = "nickname")
  private String nickname;

  @Schema(description = "모집글 제목", example = "한강 플로깅 함께해요")
  private String title;

  @Schema(description = "모집글 본문 내용", example = "함께 한강을 걸으며 쓰레기를 주워요!")
  private String content;

  @Schema(description = "활동 위치 유형", example = "CIRCLE", allowableValues = {"CIRCLE", "POLYGON"})
  private LocationType locationType;

  @Schema(description = "GeoJSON 문자열 (활동 위치 좌표)",
      example = "{\"center\":{\"lat\":36.61,\"lng\":127.28},\"radius\":392.24}")
  private String locationGeojson;

  @Schema(description = "모집 정원 (최대 참여자 수)", example = "20")
  private Integer maxParticipants;

  @Schema(description = "현재 참여자 수", example = "5")
  private Integer participantCount;

  @Schema(description = "좋아요 수", example = "12")
  private Integer likeCount;

  @Schema(description = "댓글 수", example = "3")
  private Integer commentCount;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "활동일", example = "2025-09-01T10:00:00")
  private LocalDateTime eventDate;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "신청 마감일", example = "2025-08-30T23:59:59")
  private LocalDateTime deadlineAt;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "등록일시", example = "2025-08-01T12:34:56")
  private LocalDateTime createdAt;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(description = "수정일시", example = "2025-08-10T08:22:11")
  private LocalDateTime updatedAt;

  @Schema(description = "첨부 이미지 URL 목록")
  private List<String> imageUrls;

  public static GreenTeamPostDetailResponse from(GreenTeamPost post, List<String> imageUrls) {
    return GreenTeamPostDetailResponse.builder()
        .id(post.getId())
        .userId(post.getUser().getId())
        .nickname(post.getUser().getNickname())
        .title(post.getTitle())
        .content(post.getContent())
        .locationType(post.getLocationType())
        .locationGeojson(post.getLocationGeojson())
        .maxParticipants(post.getMaxParticipants())
        .participantCount(post.getParticipantCount())
        .likeCount(post.getLikeCount())
        .commentCount(post.getCommentCount())
        .eventDate(post.getEventDate())
        .deadlineAt(post.getDeadlineAt())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .imageUrls(imageUrls)
        .build();
  }
}
