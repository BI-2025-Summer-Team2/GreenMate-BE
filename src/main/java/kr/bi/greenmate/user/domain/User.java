package kr.bi.greenmate.user.domain;

import jakarta.persistence.*;
import kr.bi.greenmate.common.domain.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR2(10 CHAR)", unique = true)
    private String nickname;

    @Column(length = 63)
    private String profileImageUrl;

    @Column(columnDefinition = "VARCHAR2(300 CHAR)")
    private String selfIntroduction;

    private LocalDateTime deletedAt;
}
