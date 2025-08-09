package kr.bi.greenmate.user.exception;

public class DuplicateEmailException extends RuntimeException{

    public DuplicateEmailException(String email) {
        super(buildMessage(email));
    }

    private static String buildMessage(String email) {
        return "Duplicate email: " + email;
    }

}
