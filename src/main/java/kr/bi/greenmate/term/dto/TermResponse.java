package kr.bi.greenmate.term.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.bi.greenmate.term.domain.Term;
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

    public static TermResponse from(Term term) {
        return builder()
                .id(term.getId())
                .title(term.getTitle())
                .content(term.getContent())
                .mandatory(term.isMandatory())
                .build();
    }

}
