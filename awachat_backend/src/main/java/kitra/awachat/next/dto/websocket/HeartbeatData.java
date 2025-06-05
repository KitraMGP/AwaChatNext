package kitra.awachat.next.dto.websocket;

/**
 * 心跳消息的数据结构
 */
public record HeartbeatData(long timestamp) {
    public static HeartbeatData create() {
        return new HeartbeatData(System.currentTimeMillis());
    }
}