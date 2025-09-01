package kr.bi.greenmate.term.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TermResponse {

    @Schema(description = "약관 id", example = "1")
    private Long id;

    @Schema(description = "약관 제목", example = "개인정보 처리방침")
    private String title;

    @Schema(description = "약관 내용", example = "개인정보 처리방침에 대한 상세 내용입니다.")
    private String content;

    @Schema(description = "필수 여부", example = "true")
    private boolean mandatory;
}
