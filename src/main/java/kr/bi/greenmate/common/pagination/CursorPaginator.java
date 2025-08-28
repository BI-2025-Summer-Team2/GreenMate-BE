package kr.bi.greenmate.common.pagination;

import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import kr.bi.greenmate.common.pagination.dto.PageInfo;
import kr.bi.greenmate.common.pagination.dto.PageResponse;
import kr.bi.greenmate.common.pagination.CursorCodec.Payload;
import org.springframework.stereotype.Component;

/**
 * 커서 기반(Keyset) 페이지네이션 오케스트레이터.
 * - 요청 size 정규화(clamp), 첫/next/prev 분기, size+1 조회로 다음 페이지 여부 판단
 * - prev는 ASC로 조회 후 화면 일관성 유지를 위해 reverse 처리
 * - next/prev 커서 생성까지 일괄 처리
 * - 레포지토리 접근은 람다 주입으로 도메인 독립적 사용
 */
@Component
public class CursorPaginator {

  /**
   * @param defaultSize  기본 페이지 크기
   * @param maxSize      허용 최대 페이지 크기
   * @param reqSize      요청된 페이지 크기(옵셔널)
   * @param cursor       커서 문자열(옵셔널, Base64 URL-safe)
   * @param fetchFirst   첫 페이지 조회: desc 정렬, (take) -> rows(desc)
   * @param fetchNext    다음 페이지 조회: (cAt, cId, take) -> rows(desc)
   * @param fetchPrevAsc 이전 페이지 조회: (cAt, cId, take) -> rows(asc)
   * @param createdAt    엔티티에서 커서 키(createdAt) 추출자
   * @param id           엔티티에서 커서 키(id) 추출자
   * @param mapper       엔티티 -> DTO 변환자
   */

  public <E, T> PageResponse<T> paginate(
      int defaultSize, int maxSize, Integer reqSize, String cursor,

      // 레포지토리 호출
      IntFunction<List<E>> fetchFirst, /* take -> rows(desc) */
      TriFunction<LocalDateTime, Long, Integer, List<E>> fetchNext, /* (cAt,cId,take)->rows(desc) */
      TriFunction<LocalDateTime, Long, Integer, List<E>> fetchPrevAsc, /* (cAt,cId,take)->rows(asc) */

      // 커서 키 추출자
      Function<E, LocalDateTime> createdAt,
      Function<E, Long> id,

      // 엔티티 -> DTO 매퍼
      Function<E, T> mapper
  ) {
    // size 정규화 및 첫 페이지 여부 판정
    final int limit = clamp(reqSize, defaultSize, maxSize);
    final boolean firstPage = (cursor == null || cursor.isBlank());

    // 데이터 조회 (size+1로 다음 페이지 존재 여부 계산)
    final List<E> rows;
    if (firstPage) {
      rows = fetchFirst.apply(limit + 1);
    } else {
      Payload p = CursorCodec.decode(cursor);
      if ("next".equals(p.getDirection())) {
        rows = fetchNext.apply(p.getCreatedAt(), p.getId(), limit + 1);
      } else {
        // prev: ASC로 가져와서 화면 노출은 최신→과거(desc)로 맞추기 위해 reverse
        rows = fetchPrevAsc.apply(p.getCreatedAt(), p.getId(), limit + 1);
        Collections.reverse(rows);
      }
    }

    // 페이지 분할 및 next 존재 여부 계산
    final boolean hasMore = rows.size() > limit;
    final List<E> page = hasMore ? rows.subList(0, limit) : rows;

    // 커서 생성 (현재 페이지의 first/last 기준)
    String next = null, prev = null;
    if (!page.isEmpty()) {
      E first = page.get(0);
      E last = page.get(page.size() - 1);

      if (!firstPage) {
        prev = CursorCodec.encode(createdAt.apply(first), id.apply(first), "prev");
      }
      if (hasMore) {
        next = CursorCodec.encode(createdAt.apply(last), id.apply(last), "next");
      }
    }

    PageInfo pageInfo = PageInfo.builder()
        .nextCursor(next)
        .prevCursor(prev)
        .build();

    return PageResponse.of(page.stream().map(mapper).collect(toList()), pageInfo);
  }

  /**
   * 요청 size를 [1, max] 범위로 정규화(요청 없으면 기본값).
   */
  private int clamp(Integer size, int def, int max) {
    if (size == null) {
      return def;
    }
    if (size < 1) {
      return 1;
    }
    return Math.min(size, max);
  }

  @FunctionalInterface
  public interface TriFunction<A, B, C, R> {

    R apply(A a, B b, C c);
  }
}
