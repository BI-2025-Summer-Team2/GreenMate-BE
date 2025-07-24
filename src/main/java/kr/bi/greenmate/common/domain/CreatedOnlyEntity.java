package kr.bi.greenmate.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class CreatedOnlyEntity {

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  protected LocalDateTime createdAt;
}
