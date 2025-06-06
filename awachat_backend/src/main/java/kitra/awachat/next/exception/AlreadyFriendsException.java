package kitra.awachat.next.exception;

public class AlreadyFriendsException extends ApiException {
    public AlreadyFriendsException() {
        super(200412, "已经是好友关系");
    }
}