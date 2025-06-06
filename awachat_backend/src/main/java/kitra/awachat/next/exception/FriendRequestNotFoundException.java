package kitra.awachat.next.exception;

public class FriendRequestNotFoundException extends ApiException {
    public FriendRequestNotFoundException() {
        super(200411, "好友请求不存在");
    }
}
