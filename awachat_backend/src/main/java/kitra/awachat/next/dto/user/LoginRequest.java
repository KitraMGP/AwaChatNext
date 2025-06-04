package kitra.awachat.next.dto.user;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull String username, @NotNull String password) {
}
