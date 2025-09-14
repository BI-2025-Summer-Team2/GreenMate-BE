package kr.bi.greenmate.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    // 400
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "GLOBAL-40001", "요청 인자값이 올바르지 않습니다."),
    MISSING_REQUIRED_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL-40002", "필수 요청 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_FORMAT(HttpStatus.BAD_REQUEST, "GLOBAL-40003", "요청 파라미터의 형식이 올바르지 않습니다."),
    INVALID_HTTP_METHOD(HttpStatus.BAD_REQUEST, "GLOBAL-40004", "지원하지 않는 HTTP 메서드입니다."),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "GLOBAL-40401", "요청한 리소스를 찾을 수 없습니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL-50001", "서버 내부에 예기치 못한 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public static ErrorCode from(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            return INVALID_ARGUMENT;
        }
        if (ex instanceof MissingServletRequestParameterException) {
            return MISSING_REQUIRED_PARAMETER;
        }
        if (ex instanceof MethodArgumentTypeMismatchException) {
            return INVALID_PARAMETER_FORMAT;
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return INVALID_HTTP_METHOD;
        }
        if (ex instanceof NoResourceFoundException) {
            return RESOURCE_NOT_FOUND;
        }
        return INTERNAL_SERVER_ERROR;
    }
}
