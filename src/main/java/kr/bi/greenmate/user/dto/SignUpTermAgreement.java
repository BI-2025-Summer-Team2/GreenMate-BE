package kr.bi.greenmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 약관 동의 상세 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpTermAgreement {

    @Schema(description = "약관 ID", example = "1")
    @NotNull(message = "termId 필수")
    @Positive(message = "termId 양수")
    private Long termId;

    @Schema(description = "약관 동의 여부", example = "true")
    @NotNull(message = "agreed 필수")
    private Boolean agreed;

}
