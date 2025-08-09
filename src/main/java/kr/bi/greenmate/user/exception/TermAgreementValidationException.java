package kr.bi.greenmate.user.exception;

public class TermAgreementValidationException extends RuntimeException {

    public TermAgreementValidationException() {
        super("Invalid term agreement request");
    }
}
