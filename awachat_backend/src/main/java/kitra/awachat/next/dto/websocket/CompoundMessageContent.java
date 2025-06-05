package kitra.awachat.next.dto.websocket;

/**
 * 复合消息内容
 */
public record CompoundMessageContent(CompoundMessagePart[] parts) {
}