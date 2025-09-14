package kr.bi.greenmate.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.bi.greenmate.community.domain.Community;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.dto.UserInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Schema(description = "게시글 상세 조회 응답 DTO")
@Getter
@Builder
public class CommunityPostDetailResponse {

    @Schema(description = "게시글 id", example = "1")
    private Long communityId;

    @Schema(description = "제목", example = "길거리 청소왕 인사 올립니다.")
    private String title;

    @Schema(description = "본문", example = "안녕하세요! 사실 아직 청소왕 꿈나무라서 선생님들께 한수 가르침을 얻고 싶습니다.")
    private String content;

    @Schema(description = "좋아요 개수", example = "0")
    private Integer likeCount;

    @Schema(description = "댓글 개수", example = "0")
    private Integer commentCount;

    @Schema(description = "첨부 이미지 url 목록")
    private List<String> imageUrls;

    @Schema(description = "생성 일시", example = "2025-08-30 20:15")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-08-30 20:15")
    private LocalDateTime updatedAt;

    @Schema(description = "작성자 정보")
    private UserInfo writer;

    @Schema(description = "사용자의 게시글 좋아요 여부", example = "false")
    private boolean isLiked;

    public static CommunityPostDetailResponse from(Community post, List<String> imageUrls, boolean isLiked) {
        User user = post.getUser();

        return CommunityPostDetailResponse.builder()
                .communityId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .imageUrls(imageUrls)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .writer(UserInfo.from(user))
                .isLiked(isLiked)
                .build();
    }
}
