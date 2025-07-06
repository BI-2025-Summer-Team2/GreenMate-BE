package kr.bi.greenmate.domain.green_team_post;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "green_team_post_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreenTeamPostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private GreenTeamPost post;

    @Column(name = "image_url", columnDefinition = "VARCHAR2(1000 CHAR)", nullable = false)
    private String imageUrl;
}