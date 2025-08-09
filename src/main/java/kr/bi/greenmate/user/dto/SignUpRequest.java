package kr.bi.greenmate.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    @NotBlank(message = "이메일 필수")
    @Email(message = "올바르지 않은 이메일 형식")
    @Size(max = 100, message = "이메일은 100자 이하")
    private String email;

    // 특수문자 "^"는 예기치 않은 오류 및 보안 취약점이 될 수 있어서 제외
    @NotBlank(message = "비밀번호 필수")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9!@#$%&*]+$",
            message = "비밀번호에 영문과 숫자 필수 포함, 특수문자는 !@#$%&*만 사용 가능"
    )
    @Size(min = 8, max = 100, message = "비밀번호 8자 이상, 100자 이하")
    private String password;

    @NotBlank(message = "비밀번호 확인 필수")
    private String passwordConfirm;

    @NotBlank(message = "닉네임 필수")
    @Size(min = 2, max = 10, message = "닉네임 2자 이상, 10자 이하")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣A-Za-z0-9]+$", message = "닉네임에 한글, 영문, 숫자만 가능")
    private String nickname;

    @Size(max = 63, message = "프로필 이미지 URL 63자 이하")
    private String profileImageUrl;

    @Size(max = 300, message = "자기소개 300자 이하")
    private String selfIntroduction;

    @NotEmpty(message = "약관 확인")
    @Valid
    private List<Agreement> agreements;

    @AssertTrue(message = "비밀번호가 일치하지 않음")
    public boolean isPasswordMatching() {
        if(password == null || passwordConfirm == null){
            return false;
        }
        return password.equals(passwordConfirm);
    }
}
