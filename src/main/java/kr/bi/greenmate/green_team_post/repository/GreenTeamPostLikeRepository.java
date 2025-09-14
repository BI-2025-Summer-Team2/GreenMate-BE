package kr.bi.greenmate.green_team_post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.bi.greenmate.green_team_post.domain.GreenTeamPostLike;

public interface GreenTeamPostLikeRepository extends JpaRepository<GreenTeamPostLike, Long> {

  boolean existsByPostIdAndUserId(Long postId, Long userId);

  Optional<GreenTeamPostLike> findByPostIdAndUserId(Long postId, Long userId);
}
