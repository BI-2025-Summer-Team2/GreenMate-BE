package kr.bi.greenmate.common.dto;

import kr.bi.greenmate.common.exception.ErrorCode;

public record ErrorResponse(int status, String code, String message) {
    public ErrorResponse(ErrorCode errorCode) {
        this(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
