package kr.bi.greenmate.community.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import kr.bi.greenmate.common.domain.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.community.domain.Community;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityComment extends BaseTimeEntity{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(nullable = false, length = 100)
    private String content;

    @OneToMany(mappedBy = "communityComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityImage> images = new ArrayList<>();

    @Column(nullable = true)
    private LocalDateTime deletedAt;
}
