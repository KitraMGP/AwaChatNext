package kitra.awachat.next.dto.websocket;

import java.util.Date;

/**
 * 聊天消息的数据结构
 */
public record ChatMessageData<T>(
    Long id,              // 消息ID
    ChatType chatType,       // 聊天类型：私聊或群聊
    ChatMessageType msgType, // 消息类型：文本或复合
    Long chatId,            // 聊天ID
    Integer from,           // 发送者ID
    Integer to,             // 接收者ID
    Long replyTo,          // 回复的消息ID
    T content,              // 消息内容，结构和 msgType 有关
    Date sentAt            // 发送时间
) {
    // 创建文本消息的工厂方法
    public static ChatMessageData<TextMessageContent> createTextMessage(Long messageId, ChatType chatType, ChatMessageType chatMessageType, Long chatId, Integer from, Integer to, Long replyTo, String text, Date sentAt) {
        return new ChatMessageData<>(messageId, chatType, chatMessageType, chatId, from, to, replyTo, new TextMessageContent(text), sentAt);
    }

    // 创建复合消息的工厂方法
    public static ChatMessageData<CompoundMessageContent> createCompoundMessage(Long messageId, ChatType chatType, ChatMessageType chatMessageType, Long chatId, Integer from, Integer to, Long replyTo, CompoundMessagePart[] parts, Date sentAt) {
        return new ChatMessageData<>(messageId, chatType, chatMessageType, chatId, from, to, replyTo, new CompoundMessageContent(parts), sentAt);
    }
}