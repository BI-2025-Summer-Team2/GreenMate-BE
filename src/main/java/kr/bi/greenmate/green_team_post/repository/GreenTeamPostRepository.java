package kr.bi.greenmate.green_team_post.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;

public interface GreenTeamPostRepository extends JpaRepository<GreenTeamPost, Long> {

  @EntityGraph(attributePaths = "user")
  @Query("select p from GreenTeamPost p where p.id = :id")
  Optional<GreenTeamPost> findByIdWithUser(@Param("id") Long id);

  // 첫 페이지 조회 (커서 없음): createdAt, id 내림차순 정렬
  @EntityGraph(attributePaths = "user")
  Slice<GreenTeamPost> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);

  // 다음 페이지 조회 (커서 있음): 지정한 id 미만 데이터, createdAt, id 내림차순 정렬
  @EntityGraph(attributePaths = "user")
  Slice<GreenTeamPost> findByIdLessThanOrderByCreatedAtDescIdDesc(Long id, Pageable pageable);
}
