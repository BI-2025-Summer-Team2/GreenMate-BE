package kr.bi.greenmate.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 400
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "FILE-40001", "원본 파일명이 유효하지 않습니다."),
    MISSING_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FILE-40002", "파일 확장자가 존재하지 않습니다."),
    UNSUPPORTED_IMAGE_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FILE-40003", "지원하지 않는 이미지 파일 확장자입니다."),
    UNREADABLE_FILE_MIME_TYPE(HttpStatus.BAD_REQUEST, "FILE-40004", "파일의 MIME 타입을 확인할 수 없습니다."),
    MIME_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "FILE-40005", "파일의 MIME 타입이 확장자와 일치하지 않습니다."),

    FILE_NOT_FOUND_TO_DELETE(HttpStatus.NOT_FOUND, "FILE-40401", "삭제할 파일을 찾을 수 없습니다."),

    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "LOCK-40901", "다른 요청이 처리 중입니다."),


    // 500
    LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "LOCK-50001", "락을 대기하는 도중 스레드가 중단되었습니다."),

    FILE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-50001", "파일을 읽는 도중 오류가 발생했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-50002", "스토리지에 파일을 업로드하는 데 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE-50003", "스토리지에서 파일을 삭제하는 데 실패했습니다."),

    FAILED_TO_EXTRACT_PATH(HttpStatus.INTERNAL_SERVER_ERROR, "URL-50002", "URL에서 경로를 추출하는 데 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
