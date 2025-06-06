package kitra.awachat.next.dto.chat;

import jakarta.annotation.Nullable;

import java.util.Date;

public record PrivateChatInfo(Long chatId, Integer userId, String username, String nickname, Date createdAt,
                              Date updatedAt,
                              @Nullable String lastMessageContent, Integer unreadCount) {
}
