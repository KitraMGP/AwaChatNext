package kitra.awachat.next.dto.image;

import jakarta.validation.constraints.NotNull;

public record GetImageUploadUrlRequest(@NotNull Long chatId, @NotNull Long fileSize, @NotNull String mimeType) {
}
