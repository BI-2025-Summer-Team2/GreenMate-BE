package kr.bi.greenmate.green_team_post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;

public interface GreenTeamPostRepository extends JpaRepository<GreenTeamPost, Long> {

  @EntityGraph(attributePaths = "images")
  Optional<GreenTeamPost> findWithImagesById(Long id);
}
