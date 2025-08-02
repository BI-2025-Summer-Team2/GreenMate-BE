package kr.bi.greenmate.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public class SoftDeleteEntity extends BaseTimeEntity{

    @Column
    private LocalDateTime deletedAt;
}
