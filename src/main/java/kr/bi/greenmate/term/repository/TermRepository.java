package kr.bi.greenmate.term.repository;

import kr.bi.greenmate.term.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {

}
