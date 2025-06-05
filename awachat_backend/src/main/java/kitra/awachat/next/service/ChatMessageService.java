package kitra.awachat.next.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitra.awachat.next.dto.websocket.ChatMessageData;
import kitra.awachat.next.dto.websocket.ChatMessageType;
import kitra.awachat.next.dto.websocket.ChatType;
import kitra.awachat.next.dto.websocket.CompoundMessageContent;
import kitra.awachat.next.dto.websocket.TextMessageContent;
import kitra.awachat.next.dto.websocket.WebSocketMessage;
import kitra.awachat.next.entity.PrivateChatEntity;
import kitra.awachat.next.entity.PrivateMessageAcknowledgeEntity;
import kitra.awachat.next.entity.PrivateMessageEntity;
import kitra.awachat.next.mapper.PrivateChatMapper;
import kitra.awachat.next.mapper.PrivateMessageAcknowledgeMapper;
import kitra.awachat.next.mapper.PrivateMessageMapper;
import kitra.awachat.next.session.WebSocketSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static kitra.awachat.next.util.DataBaseUtil.checkResult;

@Service
public class ChatMessageService {
    private final WebSocketSessionManager sessionManager;
    private final PrivateChatMapper privateChatMapper;
    private final PrivateMessageMapper privateMessageMapper;
    private final PrivateMessageAcknowledgeMapper privateMessageAcknowledgeMapper; // 新增
    private final ObjectMapper objectMapper;
    private final Logger logger = LogManager.getLogger(ChatMessageService.class);

    public ChatMessageService(WebSocketSessionManager sessionManager, PrivateChatMapper privateChatMapper, PrivateMessageMapper privateMessageMapper, PrivateMessageAcknowledgeMapper privateMessageAcknowledgeMapper) { // 新增参数
        this.sessionManager = sessionManager;
        this.privateChatMapper = privateChatMapper;
        this.privateMessageMapper = privateMessageMapper;
        this.privateMessageAcknowledgeMapper = privateMessageAcknowledgeMapper; // 新增
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 处理接收到的聊天消息
     *
     * @param senderId    发送者ID
     * @param messageData 消息数据
     * @return 是否处理成功
     */
    @Transactional
    public boolean handleChatMessage(Integer senderId, ChatMessageData<?> messageData) {
        try {
            // 1. 验证发送者ID是否匹配
            if (!senderId.equals(messageData.from())) {
                logger.warn("消息发送者ID不匹配: {} vs {}", senderId, messageData.from());
                return false;
            }

            // 2. 验证会话是否存在
            PrivateChatEntity chatEntity = privateChatMapper.selectById(messageData.chatId());
            if (chatEntity == null) {
                logger.warn("会话不存在: {}", messageData.chatId());
                return false;
            }

            // 3. 验证发送者是否属于该会话
            if (!chatEntity.getUser1Id().equals(senderId) && !chatEntity.getUser2Id().equals(senderId)) {
                logger.warn("用户 {} 不属于会话 {}", senderId, messageData.chatId());
                return false;
            }

            // 4. 保存消息到数据库
            PrivateMessageEntity messageEntity = new PrivateMessageEntity();
            messageEntity.setChatId(messageData.chatId());
            messageEntity.setSenderId(senderId);
            messageEntity.setReceiverId(messageData.to());
            messageEntity.setReplyTo(messageData.replyTo());
            
            // 设置发送时间
            Date sentAt = new Date();
            messageEntity.setSentAt(sentAt);
            messageEntity.setIsDeleted(false);

            // 设置消息内容和类型
            Map<String, Object> contentMap = new HashMap<>();
            if (messageData.msgType() == ChatMessageType.TEXT) {
                TextMessageContent textContent = (TextMessageContent) messageData.content();
                contentMap.put("text", textContent.content());
                messageEntity.setContentType((short) 0); // 文本消息
            } else if (messageData.msgType() == ChatMessageType.COMPOUND) {
                CompoundMessageContent compoundContent = (CompoundMessageContent) messageData.content();
                contentMap.put("parts", compoundContent.parts());
                messageEntity.setContentType((short) 1); // 复合消息
            } else {
                logger.warn("不支持的消息类型: {}", messageData.msgType());
                return false;
            }
            messageEntity.setContent(contentMap);

            // 保存消息
            checkResult(privateMessageMapper.insertPrivateMessage(messageEntity));

            // 5. 更新会话的最后一条消息ID和更新时间
            chatEntity.setLastMessageId(messageEntity.getMessageId());
            chatEntity.setUpdatedAt(new Date());
            privateChatMapper.updateById(chatEntity);

            // 6. 创建包含ID和发送时间的新消息数据
            ChatMessageData<?> updatedMessageData = new ChatMessageData<>(
                messageEntity.getMessageId(),
                messageData.chatType(),
                messageData.msgType(),
                messageData.chatId(),
                messageData.from(),
                messageData.to(),
                messageData.replyTo(),
                messageData.content(),
                sentAt
            );

            // 7. 转发消息给接收者
            forwardMessageToReceiver(messageData.to(), WebSocketMessage.createChatMessage(updatedMessageData));

            return true;
        } catch (Exception e) {
            logger.error("处理聊天消息时出错", e);
            return false;
        }
    }

    /**
     * 转发消息给接收者
     *
     * @param receiverId 接收者ID
     * @param message    要转发的消息
     */
    private void forwardMessageToReceiver(Integer receiverId, WebSocketMessage<?> message) {
        try {
            // 获取接收者的所有会话
            Set<WebSocketSession> receiverSessions = sessionManager.getSessionsByUser(receiverId);
            if (receiverSessions.isEmpty()) {
                logger.info("接收者 {} 不在线，消息将在其上线后发送", receiverId);
                return;
            }

            // 将消息转换为JSON字符串
            String messageJson = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(messageJson);

            // 发送消息到接收者的所有会话
            for (WebSocketSession session : receiverSessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                        logger.debug("消息已发送给接收者 {}, 会话ID: {}", receiverId, session.getId());
                    }
                } catch (IOException e) {
                    logger.error("发送消息给接收者 {} 失败", receiverId, e);
                }
            }
        } catch (Exception e) {
            logger.error("转发消息时出错", e);
        }
    }

    /**
     * 处理并存储用户的消息已读请求
     *
     * @param userId        用户ID
     * @param chatType      聊天类型（私聊或群聊）
     * @param chatId        会话ID
     * @param lastMessageId 最后一条已读消息ID
     * @return 是否处理成功
     */
    @Transactional
    public boolean handleReadAcknowledge(Integer userId, ChatType chatType, Long chatId, Long lastMessageId) {
        try {
            // 1. 验证聊天类型是否为私聊
            if (chatType != ChatType.PRIVATE) {
                logger.warn("不支持的聊天类型: {}", chatType);
                return false;
            }

            // 2. 验证会话是否存在
            PrivateChatEntity chatEntity = privateChatMapper.selectById(chatId);
            if (chatEntity == null) {
                logger.warn("会话不存在: {}", chatId);
                return false;
            }

            // 3. 验证用户是否属于该会话
            if (!chatEntity.getUser1Id().equals(userId) && !chatEntity.getUser2Id().equals(userId)) {
                logger.warn("用户 {} 不属于会话 {}", userId, chatId);
                return false;
            }

            // 4. 验证消息是否存在
            PrivateMessageEntity messageEntity = privateMessageMapper.selectById(lastMessageId);
            if (messageEntity == null) {
                logger.warn("消息不存在: {}", lastMessageId);
                return false;
            }

            // 5. 验证消息是否属于该会话
            if (!messageEntity.getChatId().equals(chatId)) {
                logger.warn("消息 {} 不属于会话 {}", lastMessageId, chatId);
                return false;
            }

            // 6. 更新或插入已读记录
            // 先查询是否已存在记录
            QueryWrapper<PrivateMessageAcknowledgeEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("chat_id", chatId).eq("user_id", userId);

            PrivateMessageAcknowledgeEntity ackEntity = privateMessageAcknowledgeMapper.selectOne(queryWrapper);

            if (ackEntity == null) {
                // 不存在记录，插入新记录
                ackEntity = new PrivateMessageAcknowledgeEntity();
                ackEntity.setChatId(chatId);
                ackEntity.setUserId(userId);
                ackEntity.setLastMessageId(lastMessageId);
                privateMessageAcknowledgeMapper.insert(ackEntity);
            } else {
                // 已存在记录，更新last_message_id
                // 只有当新的lastMessageId大于现有的lastMessageId时才更新
                if (lastMessageId > ackEntity.getLastMessageId()) {
                    ackEntity.setLastMessageId(lastMessageId);
                    privateMessageAcknowledgeMapper.updateById(ackEntity);
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("处理消息已读请求时出错", e);
            return false;
        }
    }
}