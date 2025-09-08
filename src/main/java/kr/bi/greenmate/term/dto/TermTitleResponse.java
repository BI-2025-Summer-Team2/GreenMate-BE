package kr.bi.greenmate.term.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.bi.greenmate.term.domain.Term;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "약관 요약 조회 DTO")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class TermTitleResponse {

    @Schema(description = "약관 id", example = "1")
    private Long id;

    @Schema(description = "필수 여부", example = "true")
    private boolean mandatory;

    @Schema(description = "약관 제목", example = "개인정보 처리방침")
    private String title;

    public static TermTitleResponse from(Term term) {
        return builder()
                .id(term.getId())
                .mandatory(term.isMandatory())
                .title(term.getTitle())
                .build();
    }
}
