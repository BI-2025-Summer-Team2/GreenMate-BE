package kr.bi.greenmate.green_team_post.repository;

import kr.bi.greenmate.green_team_post.domain.GreenTeamParticipant;
import kr.bi.greenmate.green_team_post.domain.GreenTeamPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GreenTeamParticipantRepository extends JpaRepository<GreenTeamParticipant, Long> {

  boolean existsByPostIdAndUserId(Long postId, Long userId);

  Optional<GreenTeamParticipant> findByPostIdAndUserId(Long postId, Long userId);

  @EntityGraph(attributePaths = {"post", "post.user"})
  @Query("select gp.post from GreenTeamParticipant gp " +
      "where gp.user.id = :userId " +
      "and (:cursorId is null or gp.post.id < :cursorId) " +
      "order by gp.post.id desc")
  Slice<GreenTeamPost> findParticipatedPosts(
      @Param("userId") Long userId,
      @Param("cursorId") Long cursorId,
      Pageable pageable
  );
}
