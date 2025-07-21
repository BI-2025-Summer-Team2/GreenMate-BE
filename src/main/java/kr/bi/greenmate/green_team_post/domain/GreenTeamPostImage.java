package kr.bi.greenmate.green_team_post.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "green_team_post_image")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreenTeamPostImage {

  @Id
  @SequenceGenerator(
      name = "greenTeamPostImageSeq",
      sequenceName = "GREEN_TEAM_POST_IMAGE_SEQ",
      allocationSize = 1
  )
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "greenTeamPostImageSeq"
  )
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "post_id", nullable = false)
  private GreenTeamPost post;

  @Column(name = "image_url", nullable = false, length = 64)
  private String imageUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
