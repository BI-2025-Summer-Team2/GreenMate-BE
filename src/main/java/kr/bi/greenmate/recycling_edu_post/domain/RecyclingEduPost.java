package kr.bi.greenmate.recycling_edu_post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.bi.greenmate.common.domain.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecyclingEduPost extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String title;

  @Column(nullable = false, length = 4000)
  private String content;

  @Column(nullable = false, length = 63)
  private String imageUrl;
}
