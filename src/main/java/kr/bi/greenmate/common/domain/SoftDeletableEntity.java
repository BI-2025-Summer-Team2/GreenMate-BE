package kr.bi.greenmate.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 소프트 삭제용 추상 클래스입니다.
 * <p>
 * - deleted_at 컬럼을 기준으로 삭제 여부를 판단합니다.
 * - 삭제는 @SQLDelete 어노테이션으로 처리되며, deleted_at에 timestamp가 기록됩니다.
 * <p>
 * 기본 조회 시 deleted_at이 NULL인지 여부는 각 쿼리 메서드에서 명시적으로 처리해야 하며,
 * 삭제된 데이터도 조회가 필요한 도메인 특성상 @SQLRestriction 어노테이션은 사용하지 않습니다.
 * <p>
 * 사용 시 각 엔티티에 아래 어노테이션을 반드시 선언해야 합니다:
 *
 * import org.hibernate.annotations.SQLDelete;
 * @SQLDelete(sql = "UPDATE 테이블명 SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
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
