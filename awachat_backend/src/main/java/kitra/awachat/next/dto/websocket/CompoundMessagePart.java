package kitra.awachat.next.dto.websocket;

/**
 * 复合消息部分
 */
public record CompoundMessagePart(CompoundMessagePartType type, String content) {
}