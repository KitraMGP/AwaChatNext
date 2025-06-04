package kitra.awachat.next.exception;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super(200404, "用户不存在");
    }
}
