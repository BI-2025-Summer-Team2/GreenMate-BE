package kr.bi.greenmate.green_team_post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.bi.greenmate.green_team_post.domain.GreenTeamPostImage;

public interface GreenTeamPostImageRepository extends JpaRepository<GreenTeamPostImage, Long> {

  List<GreenTeamPostImage> findByPostId(Long postId);
}
