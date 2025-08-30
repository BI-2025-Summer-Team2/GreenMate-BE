package kr.bi.greenmate.community.repository;

import kr.bi.greenmate.community.domain.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<Community, Long> {
}
