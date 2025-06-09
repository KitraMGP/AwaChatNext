package kitra.awachat.next.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitra.awachat.next.dto.websocket.*;
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
import java.util.*;

import static kitra.awachat.next.util.DataBaseUtil.checkResult;
import static kitra.awachat.next.util.DataBaseUtil.checkResultGreaterThanZero;

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
     * 处理接收到的聊天消息，实际功能为：将聊天消息写入数据库，然后分别转发给接收者和发送者
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
            Map<String, Object> contentMap = objectMapper.convertValue(messageData.content(), new TypeReference<>() {
            });
            messageEntity.setContent(contentMap);
            if (messageData.msgType() == ChatMessageType.TEXT) {
                messageEntity.setContentType((short) 0); // 文本消息
            } else if (messageData.msgType() == ChatMessageType.COMPOUND) {
                messageEntity.setContentType((short) 1); // 复合消息
            } else if (messageData.msgType() == ChatMessageType.FRIEND_REQUEST) {
                messageEntity.setContentType((short) 2); // 好友请求消息
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

            // 8. 转发完整消息给发送者
            forwardMessageToReceiver(messageData.from(), WebSocketMessage.createChatMessage(updatedMessageData));

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
                checkResult(privateMessageAcknowledgeMapper.insert(ackEntity));
            } else {
                // 已存在记录，更新last_message_id
                // 只有当新的lastMessageId大于现有的lastMessageId时才更新
                if (lastMessageId > ackEntity.getLastMessageId()) {
                    ackEntity.setLastMessageId(lastMessageId);
                    checkResultGreaterThanZero(privateMessageAcknowledgeMapper.update(ackEntity));
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("处理消息已读请求时出错", e);
            return false;
        }
    }


    /**
     * 获取历史消息。方法会先检查会话是否存在，检查用户是否在会话中，
     * 然后查询消息表中匹配该会话的消息（按照ID正序排列）
     *
     * @param userId        发出请求的用户ID，用于验证用户是否在会话中
     * @param chatId        会话ID
     * @param chatType      聊天类型
     * @param lastMessageId 最后一条消息ID，如果为null则获取最新消息
     * @param limit         消息数量限制
     * @return 历史消息列表，按照ID升序排列
     */
    public List<ChatMessageData<?>> getHistoryMessages(Integer userId, Long chatId, ChatType chatType, Long lastMessageId, int limit) {
        List<ChatMessageData<?>> result = new ArrayList<>();

        if (chatType == ChatType.PRIVATE) {
            // 检查用户是否在会话中，若不在则抛出权限不足异常
            QueryWrapper<PrivateChatEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("chat_id", chatId);
            PrivateChatEntity privateChatEntity = privateChatMapper.selectOne(queryWrapper);
            if (privateChatEntity == null) {
                // TODO 类似的位置需要添加把错误消息发送给客户端的功能！
                logger.warn("获取历史消息时找不到会话：{}", chatId);
                return result;
            }
            if (!privateChatEntity.getUser1Id().equals(userId) && !privateChatEntity.getUser2Id().equals(userId)) {
                // TODO 类似的位置需要添加把错误消息发送给客户端的功能！
                logger.warn("获取历史消息时用户{}不在会话{}中，权限不足", userId, chatId);
                return result;
            }
            // 构建查询条件
            QueryWrapper<PrivateMessageEntity> queryWrapper2 = new QueryWrapper<>();

            // 查询条件：接收者或发送者是当前用户
            queryWrapper2.and(wrapper -> wrapper
                .eq("chat_id", chatId));

            // 如果提供了lastMessageId，则获取比这个ID小的消息（更早的消息）
            if (lastMessageId != null && lastMessageId > 0) {
                queryWrapper2.lt("message_id", lastMessageId);
            }

            // 按消息ID降序排序，限制返回数量
            queryWrapper2.orderByDesc("message_id").last("LIMIT " + limit);

            // 执行查询
            List<PrivateMessageEntity> messages = privateMessageMapper.selectList(queryWrapper2);

            // 转换为ChatMessageData格式
            for (PrivateMessageEntity message : messages) {
                ChatMessageData<?> chatMessageData = convertToChatMessageData(message);
                if (chatMessageData != null) {
                    result.add(chatMessageData);
                }
            }

            // 反转列表，使消息按时间升序排列
            Collections.reverse(result);
        }

        return result;
    }

    /**
     * 将PrivateMessageEntity转换为ChatMessageData
     */
    private ChatMessageData<?> convertToChatMessageData(PrivateMessageEntity message) {
        try {
            ChatMessageType msgType;
            Object content;

            // 根据contentType确定消息类型和内容
            switch (message.getContentType()) {
                case 0: // 文本消息
                    msgType = ChatMessageType.TEXT;
                    content = objectMapper.convertValue(message.getContent(), TextMessageContent.class);
                    break;

                case 1: // 复合消息
                    msgType = ChatMessageType.COMPOUND;
                    content = objectMapper.convertValue(message.getContent(), CompoundMessageContent.class);
                    logger.warn("暂时不支持复合消息");
                    break;

                case 2: // 好友请求消息
                    msgType = ChatMessageType.FRIEND_REQUEST;
                    content = objectMapper.convertValue(message.getContent(), FriendRequestMessageContent.class);
                    break;

                default:
                    logger.warn("未知的消息类型: {}", message.getContentType());
                    return null;
            }

            // 创建ChatMessageData对象
            return new ChatMessageData<>(
                message.getMessageId(),
                ChatType.PRIVATE,
                msgType,
                message.getChatId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getReplyTo(),
                content,
                message.getSentAt()
            );
        } catch (Exception e) {
            logger.error("转换消息格式时出错", e);
            return null;
        }
    }
}