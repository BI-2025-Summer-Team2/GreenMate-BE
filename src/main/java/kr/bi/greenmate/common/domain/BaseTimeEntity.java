package kr.bi.greenmate.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends CreatedOnlyEntity {

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  protected LocalDateTime updatedAt;
}
