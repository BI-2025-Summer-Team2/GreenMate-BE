package kr.bi.greenmate.community.repository;

import kr.bi.greenmate.community.domain.Community;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    @EntityGraph(attributePaths = {"user", "images"})
    Optional<Community> findWithDetailsById(Long id);
}
