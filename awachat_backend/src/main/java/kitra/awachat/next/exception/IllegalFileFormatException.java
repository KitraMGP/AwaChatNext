package kitra.awachat.next.exception;

public class IllegalFileFormatException extends ApiException {
    public IllegalFileFormatException() {
        super(200407, "非法的文件格式");
    }
}
