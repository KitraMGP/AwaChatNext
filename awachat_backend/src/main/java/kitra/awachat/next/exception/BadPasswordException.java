package kitra.awachat.next.exception;

public class BadPasswordException extends ApiException {
    public BadPasswordException() {
        super(200403, "密码错误");
    }
}
