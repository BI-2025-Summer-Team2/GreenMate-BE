package kr.bi.greenmate.community.repository;

import kr.bi.greenmate.community.domain.CommunityComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
}
