package kr.bi.greenmate.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.bi.greenmate.common.exception.ErrorCode;

@Schema(description = "예외 발생 시 응답 DTO")
public record ErrorResponse(int status, String code, String message) {
    public ErrorResponse(ErrorCode errorCode) {
        this(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
