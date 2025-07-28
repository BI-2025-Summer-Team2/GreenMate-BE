package kr.bi.greenmate.user.domain;

import jakarta.persistence.*;
import kr.bi.greenmate.common.domain.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NaturalIdCache
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 10, unique = true)
    private String nickname;

    @Column(length = 63)
    private String profileImageUrl;

    @Column(length = 300)
    private String selfIntroduction;

    private LocalDateTime deletedAt;
}
