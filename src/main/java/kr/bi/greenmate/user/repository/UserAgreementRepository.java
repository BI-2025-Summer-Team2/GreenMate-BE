package kr.bi.greenmate.user.repository;

import kr.bi.greenmate.user.domain.UserAgreement;
import kr.bi.greenmate.user.domain.UserAgreementId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, UserAgreementId> {
}
