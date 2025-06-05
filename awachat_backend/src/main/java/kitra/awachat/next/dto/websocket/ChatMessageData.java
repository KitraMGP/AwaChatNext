package kitra.awachat.next.dto.websocket;

/**
 * 聊天消息的数据结构
 */
public record ChatMessageData(
    String msgType,           // 消息类型：text, compound等
    Long conversationId,      // 聊天对话ID
    Integer from,             // 发送者ID
    Integer to,               // 接收者ID
    Long replyTo,             // 回复的消息ID
    Object content            // 消息内容，结构和msgType有关
) {
    // 消息类型常量
    public static final String MSG_TYPE_TEXT = "text";
    public static final String MSG_TYPE_COMPOUND = "compound";

    // 创建文本消息的工厂方法
    public static ChatMessageData createTextMessage(Long conversationId, Integer from, Integer to, String text) {
        return new ChatMessageData(MSG_TYPE_TEXT, conversationId, from, to, null, text);
    }

    // 创建回复文本消息的工厂方法
    public static ChatMessageData createReplyTextMessage(Long conversationId, Integer from, Integer to, Long replyTo, String text) {
        return new ChatMessageData(MSG_TYPE_TEXT, conversationId, from, to, replyTo, text);
    }
}