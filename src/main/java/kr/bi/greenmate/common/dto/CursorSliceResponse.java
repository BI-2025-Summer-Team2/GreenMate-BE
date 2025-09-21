package kr.bi.greenmate.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Slice;

/**
 * 커서 기반 페이지네이션 응답 DTO
 */
@Getter
@AllArgsConstructor(staticName = "of")
@Schema(description = "커서 기반 페이지네이션 응답 DTO")
public class CursorSliceResponse<T> {

  @Schema(description = "조회된 데이터 목록")
  private final List<T> content;

  @Schema(description = "다음 페이지 존재 여부", example = "true")
  private final boolean hasNext;

  @Schema(description = "다음 페이지 요청에 사용할 커서(ID). 데이터가 없으면 null", example = "101")
  private final Long nextCursor;

  @Schema(description = "요청한 size 값", example = "10")
  private final int size;

  public static <T> CursorSliceResponse<T> from(Slice<T> slice, int size,
      Function<T, Long> idExtractor) {
    List<T> content = slice.getContent();
    Long nextCursor = content.isEmpty() ? null : idExtractor.apply(content.get(content.size() - 1));
    return new CursorSliceResponse<>(content, slice.hasNext(), nextCursor, size);
  }
}
