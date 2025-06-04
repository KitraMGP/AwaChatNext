package kitra.awachat.next.exception;

public class UserAlreadyExistsException extends ApiException {
    public UserAlreadyExistsException() {
        super(200405, "用户已存在");
    }
}
