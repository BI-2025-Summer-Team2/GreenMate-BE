package kr.bi.greenmate.green_team_post.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;

public interface GreenTeamPostRepository extends JpaRepository<GreenTeamPost, Long> {

  /**
   * 단건 조회 시 N+1 문제 방지를 위해 user 정보를 즉시 로딩
   */
  @EntityGraph(attributePaths = "user")
  @Query("select p from GreenTeamPost p where p.id = :id")
  Optional<GreenTeamPost> findByIdWithUser(@Param("id") Long id);

  /**
   * 첫 페이지 조회
   * - createdAt desc, id desc 기준 정렬
   * - 요청 size + 1개를 조회하여 다음 페이지 존재 여부 확인
   */
  @EntityGraph(attributePaths = "user")
  @Query("""
      select p
      from GreenTeamPost p
      order by p.createdAt desc, p.id desc
      """)
  List<GreenTeamPost> fetchFirst(Pageable pageable);

  /**
   * 다음 페이지 조회 (direction = next)
   * - 기준 커서 (createdAt, id)보다 과거 데이터만 조회
   * - createdAt desc, id desc 정렬
   */
  @EntityGraph(attributePaths = "user")
  @Query("""
      select p
      from GreenTeamPost p
      where
        (p.createdAt < :cAt)
        or (p.createdAt = :cAt and p.id < :cId)
      order by p.createdAt desc, p.id desc
      """)
  List<GreenTeamPost> fetchNext(@Param("cAt") LocalDateTime cAt,
      @Param("cId") Long cId,
      Pageable pageable);

  /**
   * 이전 페이지 조회 (direction = prev)
   * - 기준 커서 (createdAt, id)보다 최신 데이터만 조회
   * - createdAt asc, id asc 정렬 후
   *   서비스 레이어에서 reverse() 처리하여 desc 순서로 정렬
   */
  @EntityGraph(attributePaths = "user")
  @Query("""
      select p
      from GreenTeamPost p
      where
        (p.createdAt > :cAt)
        or (p.createdAt = :cAt and p.id > :cId)
      order by p.createdAt asc, p.id asc
      """)
  List<GreenTeamPost> fetchPrevAsc(@Param("cAt") LocalDateTime cAt,
      @Param("cId") Long cId,
      Pageable pageable);
}
