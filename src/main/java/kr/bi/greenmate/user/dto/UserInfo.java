package kr.bi.greenmate.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfo {

    @Schema(description = "사용자 id", example = "1")
    private Long id;

    @Schema(description = "사용자 닉네임", example = "그린Mate")
    private String nickname;

    @Schema(description = "사용자 프로필 사진 url")
    private String profileImage;

    private static final String DELETED_USER_NICKNAME = "알 수 없음";

    public static UserInfo deleted(){
        return UserInfo.builder()
                .nickname(DELETED_USER_NICKNAME)
                .build();
    }
}
