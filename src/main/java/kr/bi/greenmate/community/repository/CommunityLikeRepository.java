package kr.bi.greenmate.community.repository;

import kr.bi.greenmate.community.domain.CommunityLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {
    boolean existsByCommunity_IdAndUser_Id(Long communityId, Long userId);

    Optional<CommunityLike> findByCommunity_IdAndUser_Id(Long communityId, Long userId);
}
