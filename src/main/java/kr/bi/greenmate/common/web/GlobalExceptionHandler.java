package kr.bi.greenmate.common.web;

import jakarta.validation.ConstraintViolationException;
import kr.bi.greenmate.user.exception.DuplicateEmailException;
import kr.bi.greenmate.user.exception.RequiredTermNotAgreedException;
import kr.bi.greenmate.user.exception.TermAgreementValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: @RequestBody DTO 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request"));
    }

    // 400: @ModelAttribute 바인딩/검증 실패
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        log.warn("Bind failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request"));
    }

    // 400: 메서드 파라미터 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request"));
    }

    // 400: JSON 파싱/타입 매핑 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Message not readable: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid request"));
    }

    // 409: DB 무결성 위반(UNIQUE 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Constraint violation"));
    }

    // 409: 이메일 중복
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException ex) {
        log.warn("Duplicate email");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Duplicate email"));
    }

    // 400: 약관 요청 형식/일치 검증 실패
    @ExceptionHandler(TermAgreementValidationException.class)
    public ResponseEntity<ErrorResponse> handleTermAgreementValidation(TermAgreementValidationException ex) {
        log.warn("Term agreement validation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Invalid term agreement"));
    }

    // 422: 필수 약관 미동의
    @ExceptionHandler(RequiredTermNotAgreedException.class)
    public ResponseEntity<ErrorResponse> handleRequiredTerm(RequiredTermNotAgreedException ex) {
        log.warn("Required terms not agreed: {}", ex.getMessage());
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse("Required terms not agreed"));
    }

    // 500: 그 외 예기치 못한 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
    }
}
