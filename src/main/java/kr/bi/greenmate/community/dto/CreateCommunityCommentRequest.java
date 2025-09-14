package kr.bi.greenmate.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "커뮤니티 댓글 요청 DTO")
@Getter
@Builder
public class CreateCommunityCommentRequest {

    @Schema(description = "게시글 ID", example = "1")
    @Positive
    @NotNull(message = "게시글 ID 필수")
    private Long postId;

    @Schema(description = "댓글 내용", example = "안녕하세요! 같이 열심히 청소해봅시다~")
    @Size(max = 100, message = "커뮤니티 댓글 100자 이하")
    @NotBlank(message = "댓글 내용 필수")
    private String content;
}
