package kitra.awachat.next.dto.websocket;

/**
 * 后端返回给前端用户的消息历史记录
 */
public record ChatHistoryData(ChatMessageData<?>[] history) {
}
