package kitra.awachat.next.dto.websocket;

/**
 * 用于前端请求后端发送历史聊天记录
 */
public record RequestChatHistoryData(ChatType chatType, Long chatId, Long lastMessageId) {
}
