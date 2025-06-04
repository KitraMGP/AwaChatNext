package kitra.awachat.next.exception;

public class PermissionDeniedException extends ApiException {
    public PermissionDeniedException() {
        super(200408, "权限不足");
    }
}
