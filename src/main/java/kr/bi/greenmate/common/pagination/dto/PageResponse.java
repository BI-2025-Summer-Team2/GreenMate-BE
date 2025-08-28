package kr.bi.greenmate.common.pagination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "커서 기반 페이지네이션 응답 DTO")
public class PageResponse<T> {

  @Schema(description = "목록 데이터")
  private final List<T> items;

  @Schema(description = "페이지네이션 정보")
  private final PageInfo pageInfo;

  public static <T> PageResponse<T> of(List<T> items, PageInfo pageInfo) {
    return PageResponse.<T>builder()
        .items(items)
        .pageInfo(pageInfo)
        .build();
  }
}
