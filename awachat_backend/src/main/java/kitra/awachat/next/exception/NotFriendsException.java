package kitra.awachat.next.exception;

public class NotFriendsException extends ApiException {
    public NotFriendsException() {
        super(200413, "不是好友关系");
    }
}
