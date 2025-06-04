package kitra.awachat.next.dto.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserInfoRequest(@NotNull String username, @NotNull String nickname, @NotNull String description) {
}
