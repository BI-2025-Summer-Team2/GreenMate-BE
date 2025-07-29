package kr.bi.greenmate.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 소프트 삭제용 추상 클래스입니다.
 * <p>
 * - deleted_at 컬럼을 기준으로 삭제 여부를 판단합니다.
 * @Where(clause = "deleted_at IS NULL")을 통해 기본 조회에서 삭제된 데이터는 제외됩니다.
 * <p>
 * 사용 시 각 엔티티에 아래 어노테이션을 반드시 선언해야 합니다:
 *
 * @org.hibernate.annotations.SQLDelete(sql = "UPDATE 테이블명 SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
 */
@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseTimeEntity {

  @Column
  protected LocalDateTime deletedAt;

  public boolean isDeleted() {
    return deletedAt != null;
  }
}
