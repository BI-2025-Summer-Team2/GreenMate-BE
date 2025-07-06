package kr.bi.greenmate.domain.green_team_post;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import kr.bi.greenmate.domain.user.User;

@Entity
@Table(name = "green_team_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GreenTeamPost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "green_team_post_seq")
    @SequenceGenerator(name = "green_team_post_seq", sequenceName = "green_team_post_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "title", columnDefinition = "VARCHAR2(50 CHAR)", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "VARCHAR2(4000 CHAR)", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", columnDefinition = "VARCHAR2(20 CHAR)", nullable = false)
    private LocationType locationType;

    @Column(name = "location_geojson", columnDefinition = "CLOB")
    private String locationGeojson;

    @Column(name = "max_participants", columnDefinition = "NUMBER(5)", nullable = false)
    private Integer maxParticipants;

    @Column(name = "participant_count", columnDefinition = "NUMBER(5) DEFAULT 0")
    private Integer participantCount;

    @Column(name = "like_count", columnDefinition = "NUMBER(5) DEFAULT 0")
    private Integer likeCount;

    @Column(name = "comment_count", columnDefinition = "NUMBER(5) DEFAULT 0")
    private Integer commentCount;

    @Column(name = "deadline_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime deadlineAt;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", columnDefinition = "NUMBER(1) DEFAULT 0", nullable = false)
    private boolean isDeleted;

    @Column(name = "is_closed", columnDefinition = "NUMBER(1) DEFAULT 0", nullable = false)
    private boolean isClosed;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GreenTeamPostImage> images;

}