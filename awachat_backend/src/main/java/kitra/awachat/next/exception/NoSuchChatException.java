package kitra.awachat.next.exception;

public class NoSuchChatException extends ApiException {
    public NoSuchChatException() {
        super(200414, "会话不存在");
    }
}
