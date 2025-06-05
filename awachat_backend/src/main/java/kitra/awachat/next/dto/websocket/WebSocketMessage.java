package kitra.awachat.next.dto.websocket;

/**
 * WebSocket通讯的基础消息格式
 */
public record WebSocketMessage<T>(String type, T data) {
    // 消息类型常量
    public static final String TYPE_CHAT = "chat";
    public static final String TYPE_HEARTBEAT = "heartbeat";

    // 创建聊天消息的工厂方法
    public static <T> WebSocketMessage<T> createChatMessage(T data) {
        return new WebSocketMessage<>(TYPE_CHAT, data);
    }

    // 创建心跳消息的工厂方法
    public static WebSocketMessage<Void> createHeartbeatMessage() {
        return new WebSocketMessage<>(TYPE_HEARTBEAT, null);
    }
}