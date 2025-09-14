package kr.bi.greenmate.green_team_post.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.bi.greenmate.common.domain.BaseTimeEntity;
import kr.bi.greenmate.user.domain.User;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreenTeamPost extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 50)
  private String title;

  @Column(nullable = false, length = 4000)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private LocationType locationType;

  @Lob
  @Column(nullable = false)
  private String locationGeojson;

  @Column(nullable = false)
  private Integer maxParticipants;

  @Builder.Default
  @Column(nullable = false)
  private Integer participantCount = 0;

  @Builder.Default
  @Column(nullable = false)
  private Integer likeCount = 0;

  @Builder.Default
  @Column(nullable = false)
  private Integer commentCount = 0;

  @Column(nullable = false)
  private LocalDateTime eventDate;

  @Column(nullable = false)
  private LocalDateTime deadlineAt;

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GreenTeamPostImage> images = new ArrayList<>();

  public void increaseLikeCount() {
    if (this.likeCount == null) {
      this.likeCount = 0;
    }
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    if (this.likeCount == null || this.likeCount <= 0) {
      this.likeCount = 0;
    } else {
      this.likeCount--;
    }
  }
}
