package kr.bi.greenmate.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilePath {
    USER_PROFILE("/user/profile"),
    COMMUNITY_POST("/community");

    private final String path;
}
