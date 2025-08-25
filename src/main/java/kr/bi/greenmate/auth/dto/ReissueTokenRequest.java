package kr.bi.greenmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "access token 재발행 요청 DTO")
@Getter
@NoArgsConstructor
public class ReissueTokenRequest {

    @Schema(description = "refresh token", example = "....")
    @NotBlank(message = "refresh token 필수")
    private String refreshToken;
}
