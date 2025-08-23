package kr.bi.greenmate.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record IdResponse(

    @Schema(description = "생성된 리소스의 ID", example = "1")
    Long id
) {

}
