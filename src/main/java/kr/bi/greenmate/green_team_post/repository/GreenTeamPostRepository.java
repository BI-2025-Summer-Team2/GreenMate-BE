package kr.bi.greenmate.green_team_post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;

public interface GreenTeamPostRepository extends JpaRepository<GreenTeamPost, Long> {

  @EntityGraph(attributePaths = "user")
  @Query("select p from GreenTeamPost p where p.id = :id")
  Optional<GreenTeamPost> findByIdWithUser(@Param("id") Long id);

  @EntityGraph(attributePaths = "user")
  List<GreenTeamPost> findAllByOrderByCreatedAtDesc();
}
