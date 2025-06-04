package kitra.awachat.next.exception;

public class DatabaseOperationException extends ApiException {
    public DatabaseOperationException() {
        super(200501, "数据库操作异常");
    }
}
