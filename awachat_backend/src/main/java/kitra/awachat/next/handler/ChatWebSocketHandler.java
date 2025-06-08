package kitra.awachat.next.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitra.awachat.next.dto.websocket.*;
import kitra.awachat.next.entity.PrivateChatEntity;
import kitra.awachat.next.service.ChatMessageService;
import kitra.awachat.next.service.ChatService;
import kitra.awachat.next.service.FriendService;
import kitra.awachat.next.session.WebSocketSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

/**
 * Handler 用于对连接的建立、断开和消息的接收进行响应，处理所有的 WebSocket 请求
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ChatMessageService chatMessageService;
    private final ChatService chatService;
    private final FriendService friendService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LogManager.getLogger(ChatWebSocketHandler.class);

    public ChatWebSocketHandler(WebSocketSessionManager sessionManager, ChatMessageService chatMessageService,
                                ChatService chatService, FriendService friendService) {
        this.sessionManager = sessionManager;
        this.chatMessageService = chatMessageService;
        this.chatService = chatService;
        this.friendService = friendService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从会话属性获取用户ID
        Integer userId = (Integer) session.getAttributes().get("userId");

        if (userId != null) {
            // 添加到会话管理器
            sessionManager.addSession(userId, session);
            logger.info("用户连接：{}，会话ID：{}", userId, session.getId());

            // 发送欢迎消息
            try {
                WebSocketMessage<String> welcomeMessage = new WebSocketMessage<>(WebSocketMessage.TYPE_SYSTEM, "连接已建立");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
            } catch (IOException e) {
                logger.error("发送欢迎消息失败", e);
            }
        } else {
            // 未认证的连接立即关闭
            try {
                session.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Integer userId = sessionManager.getUserIdBySession(session.getId());
        if (userId == null) {
            logger.warn("收到未认证会话的消息：{}", session.getId());
            return;
        }

        try {
            // 解析消息
            String payload = message.getPayload();
            logger.debug("收到消息：{}", payload);

            // 将JSON字符串转换为WebSocketMessage对象
            WebSocketMessage<?> webSocketMessage = objectMapper.readValue(payload, WebSocketMessage.class);
            String messageType = webSocketMessage.type();

            // 根据消息类型处理
            switch (messageType) {
                case WebSocketMessage.TYPE_CHAT:
                    handleChatMessage(userId, webSocketMessage, session);
                    break;
                case WebSocketMessage.TYPE_HEARTBEAT:
                    handleHeartbeatMessage(userId, session);
                    break;
                case WebSocketMessage.TYPE_ACK:
                    handleAcknowledgeMessage(userId, webSocketMessage, session);
                    break;
                case WebSocketMessage.TYPE_REQUEST_CHAT_HISTORY:
                    handleRequestChatHistory(userId, webSocketMessage, session);
                    break;
                default:
                    logger.warn("不支持的消息类型：{}", messageType);
                    sendErrorMessage(session, "不支持的消息类型：" + messageType);
            }
        } catch (JsonProcessingException e) {
            logger.error("解析消息失败：{}", e.getMessage());
            sendErrorMessage(session, "消息格式错误");
        } catch (Exception e) {
            logger.error("处理消息时出错", e);
            sendErrorMessage(session, "服务器内部错误");
        }
    }

    /**
     * 处理用户拉取聊天历史的请求
     */
    private void handleRequestChatHistory(Integer userId, WebSocketMessage<?> webSocketMessage, WebSocketSession session) {
        try {
            // 将data部分转换为RequestChatHistoryData
            RequestChatHistoryData requestData = objectMapper.convertValue(webSocketMessage.data(), RequestChatHistoryData.class);

            // 获取历史消息
            List<ChatMessageData<?>> historyMessages = chatMessageService.getHistoryMessages(
                userId,
                requestData.chatType(),
                requestData.lastMessageId(),
                20 // 每次获取20条消息
            );

            ChatHistoryData chatHistoryData = new ChatHistoryData(historyMessages.toArray(new ChatMessageData[0]));

            // 发送历史消息给客户端
            WebSocketMessage<ChatHistoryData> response =
                new WebSocketMessage<>(WebSocketMessage.TYPE_REQUEST_CHAT_HISTORY, chatHistoryData);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("处理历史消息请求时出错", e);
            sendErrorMessage(session, "获取历史消息失败");
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Integer userId, WebSocketMessage<?> message, WebSocketSession session) {
        try {
            // 将data部分转换为ChatMessageData
            ChatMessageData<?> chatMessageData = objectMapper.convertValue(message.data(), ChatMessageData.class);

            // 1. 验证发送者ID是否匹配
            if (!userId.equals(chatMessageData.from())) {
                logger.warn("消息发送者ID不匹配: {} vs {}", userId, chatMessageData.from());
                sendErrorMessage(session, "消息发送者ID不匹配");
                return;
            }

            // 2. 检查两人是否是好友
            boolean areFriends = friendService.areFriends(chatMessageData.from(), chatMessageData.to());

            // 如果不是好友，只允许发送好友请求消息
            if (!areFriends && chatMessageData.msgType() != ChatMessageType.FRIEND_REQUEST) {
                logger.warn("非好友关系，不允许发送普通消息: 从 {} 到 {}", chatMessageData.from(), chatMessageData.to());
                sendErrorMessage(session, "您还不是对方的好友，请先发送好友请求");
                return;
            }

            // 3. 检查chatId，如果不存在则创建新会话
            // 创建或获取私聊会话
            PrivateChatEntity chatEntity = chatService.createOrGetPrivateChat(chatMessageData.from(), chatMessageData.to());

            // 更新chatId
            chatMessageData = new ChatMessageData<>(
                0L, // 目前还没有生成消息ID
                chatMessageData.chatType(),
                chatMessageData.msgType(),
                chatEntity.getChatId(),  // 使用新的chatId
                chatMessageData.from(),
                chatMessageData.to(),
                chatMessageData.replyTo(),
                chatMessageData.content(),
                chatMessageData.sentAt()
            );

            // 4. 处理聊天消息
            boolean success = chatMessageService.handleChatMessage(userId, chatMessageData);

            // 发送处理结果
            if (!success) {
                sendErrorMessage(session, "消息处理失败");
            }
        } catch (Exception e) {
            logger.error("处理聊天消息时出错", e);
            sendErrorMessage(session, "消息格式错误");
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage(Integer userId, WebSocketSession session) {
        try {
            // 回复心跳消息
            WebSocketMessage<HeartbeatData> heartbeatResponse =
                new WebSocketMessage<>(WebSocketMessage.TYPE_HEARTBEAT, HeartbeatData.create());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(heartbeatResponse)));
            logger.debug("已回复用户 {} 的心跳消息", userId);
        } catch (IOException e) {
            logger.error("发送心跳响应失败", e);
        }
    }

    /**
     * 处理用户已读反馈
     */
    private void handleAcknowledgeMessage(Integer userId, WebSocketMessage<?> message, WebSocketSession session) {
        try {
            // 将data部分转换为ChatMessageData
            ReadAcknowledgeData readAckData = objectMapper.convertValue(message.data(), ReadAcknowledgeData.class);

            // 处理聊天消息
            boolean success = chatMessageService.handleReadAcknowledge(userId, readAckData.chatType(), readAckData.chatId(), readAckData.lastMessageId());

            // 发送处理结果
            if (!success) {
                sendErrorMessage(session, "消息处理失败");
            }
        } catch (Exception e) {
            logger.error("处理聊天消息时出错", e);
            sendErrorMessage(session, "消息格式错误");
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            WebSocketMessage<String> errorResponse = new WebSocketMessage<>(WebSocketMessage.TYPE_ERROR, errorMessage);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        } catch (IOException e) {
            logger.error("发送错误消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 连接关闭时清理会话
        sessionManager.removeSession(session);
        logger.info("连接关闭：{}，原因：{}", session.getId(), status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // 处理传输错误
        sessionManager.removeSession(session);
        logger.error("传输错误：{}，错误：{}", session.getId(), exception.getMessage());
    }
}
