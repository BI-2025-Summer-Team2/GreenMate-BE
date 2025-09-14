package kr.bi.greenmate.community.repository;

import kr.bi.greenmate.community.domain.CommunityLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {
    boolean existsByCommunity_IdAndUser_Id(Long communityId, Long userId);
}
