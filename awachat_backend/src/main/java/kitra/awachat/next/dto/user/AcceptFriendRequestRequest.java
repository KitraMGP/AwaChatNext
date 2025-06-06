package kitra.awachat.next.dto.user;

import jakarta.validation.constraints.NotNull;

public record AcceptFriendRequestRequest(@NotNull Integer originUserId) {
}
