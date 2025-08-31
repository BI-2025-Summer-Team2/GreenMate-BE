package kr.bi.greenmate.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "게시글 등록 요청 DTO")
@Getter
@Builder
public class CreateCommunityPostRequest {

    @Schema(description = "게시글 제목", example = "길거리 청소왕 인사 올립니다.")
    @Size(max = 20, message = "커뮤니티 게시글 제목 20자 이하")
    @NotBlank(message = "게시글 제목 필수")
    private String title;

    @Schema(description = "게시글 내용", example = "안녕하세요! 사실 아직 청소왕 꿈나무라서 선생님들께 한수 가르침을 얻고 싶습니다.")
    @Size(max = 500, message = "커뮤니티 게시글 내용 500자 이하")
    @NotBlank(message = "게시글 내용 필수")
    private String content;
}
