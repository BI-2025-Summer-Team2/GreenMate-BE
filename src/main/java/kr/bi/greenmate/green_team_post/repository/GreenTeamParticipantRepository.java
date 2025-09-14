package kr.bi.greenmate.green_team_post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.bi.greenmate.green_team_post.domain.GreenTeamParticipant;

public interface GreenTeamParticipantRepository extends JpaRepository<GreenTeamParticipant, Long> {

  boolean existsByPostIdAndUserId(Long postId, Long userId);

  Optional<GreenTeamParticipant> findByPostIdAndUserId(Long postId, Long userId);
}
