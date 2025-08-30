package kr.bi.greenmate.community.repository;

import kr.bi.greenmate.community.domain.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {
}
