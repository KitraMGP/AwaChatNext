package kitra.awachat.next.exception;

public class LoginFailedException extends ApiException {
    public LoginFailedException() {
        super(200403, "用户名或密码错误");
    }
}
