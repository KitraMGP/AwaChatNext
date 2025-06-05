package kitra.awachat.next.dto.websocket;

public record ReadAcknowledgeData(ChatType chatType, Long chatId, Long lastMessageId) {
}
