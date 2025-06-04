package kitra.awachat.next.exception;

public class FileSizeExceededLimitException extends ApiException {
    public FileSizeExceededLimitException(String limit) {
        super(200406, "文件长度超出限制：" + limit);
    }
}
