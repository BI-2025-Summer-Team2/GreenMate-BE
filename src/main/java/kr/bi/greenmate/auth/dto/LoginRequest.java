package kr.bi.greenmate.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @Schema(description = "email", example = "user@example.com")
    @NotBlank(message = "이메일 필수")
    @Email(message = "올바르지 않은 이메일 형식")
    private String email;

    @Schema(description = "비밀번호", example = "password!@123")
    @NotBlank(message = "비밀번호 필수")
    private String password;
}
