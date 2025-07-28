package kr.bi.greenmate.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseTimeEntity {

  @Column
  protected LocalDateTime deletedAt;

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }
}