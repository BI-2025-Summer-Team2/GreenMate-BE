package kr.bi.greenmate.user.exception;

public class RequiredTermNotAgreedException extends RuntimeException{

    public RequiredTermNotAgreedException() {
        super("Required terms not agreed");
    }

}
