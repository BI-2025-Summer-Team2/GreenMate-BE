package kr.bi.greenmate.user.exception;

public class DuplicateNicknameException extends RuntimeException {
    public DuplicateNicknameException(String nickname) {
        super(buildMessage(nickname));
    }

    private static String buildMessage(String nickname) {
        return "Duplicate nickname: " + nickname;
    }
}
