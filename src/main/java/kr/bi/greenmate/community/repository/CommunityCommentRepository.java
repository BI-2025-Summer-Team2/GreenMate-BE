package kr.bi.greenmate.community.repository;

import kr.bi.greenmate.community.domain.CommunityComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

    @EntityGraph(attributePaths = "user")
    Slice<CommunityComment> findAllByCommunity_Id(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Slice<CommunityComment> findAllByCommunity_IdAndIdLessThan(Long postId, Long cursor, Pageable pageable);
}
