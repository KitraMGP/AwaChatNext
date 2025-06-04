package kitra.awachat.next.exception;

public class BadInputException extends ApiException {
    public BadInputException(String reason) {
        super(200402, "输入数据不合法：" + reason);
    }
}
