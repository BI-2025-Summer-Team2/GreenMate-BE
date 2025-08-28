package kr.bi.greenmate.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "페이지네이션 커서 정보")
public class PageInfo {

  @Schema(description = "다음 페이지 요청용 커서 (존재하지 않을 경우 null)")
  private final String nextCursor;

  @Schema(description = "이전 페이지 요청용 커서 (존재하지 않을 경우 null)")
  private final String prevCursor;
}
