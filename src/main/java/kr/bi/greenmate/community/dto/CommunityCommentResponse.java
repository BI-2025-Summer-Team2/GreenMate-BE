package kr.bi.greenmate.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.bi.greenmate.community.domain.CommunityComment;
import kr.bi.greenmate.user.domain.User;
import kr.bi.greenmate.user.dto.UserInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "커뮤니티 댓글 조회 응답 DTO")
@Getter
@Builder
public class CommunityCommentResponse {

    @Schema(description = "댓글 ID", example = "2")
    private Long commentId;

    @Schema(description = "댓글 내용", example = "안녕하세요! 사실 아직 청소왕 꿈나무라서 선생님들께 한수 가르침을 얻고 싶습니다.")
    private String content;

    @Schema(description = "댓글에 포함된 이미지 url")
    private String imageUrl;

    @Schema(description = "작성 일시", example = "2025-08-30T20:15:24.300132")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-08-30T20:15:24.300132")
    private LocalDateTime updatedAt;

    @Schema(description = "작성자 정보")
    private UserInfo writer;

    private static final String DELETED_COMMUNITY_COMMENT_CONTENT = "삭제된 댓글입니다.";

    public static CommunityCommentResponse from(CommunityComment comment){
        if(comment.getDeletedAt() == null){
            return CommunityCommentResponse.builder()
                    .content(DELETED_COMMUNITY_COMMENT_CONTENT)
                    .writer(UserInfo.deleted())
                    .build();
        }

        User writer = comment.getUser();
        return CommunityCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .imageUrl(comment.getImageUrl())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .writer(UserInfo.builder()
                        .id(writer.getId())
                        .nickname(writer.getNickname())
                        .profileImage(writer.getProfileImageUrl())
                        .build())
                .build();
    }
}
