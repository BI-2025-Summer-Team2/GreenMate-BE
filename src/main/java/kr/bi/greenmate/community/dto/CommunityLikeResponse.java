package kr.bi.greenmate.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityLikeResponse {
    private int likeCount;
    private boolean isLiked;
}
