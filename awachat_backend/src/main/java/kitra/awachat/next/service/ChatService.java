package kitra.awachat.next.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kitra.awachat.next.dto.chat.ChatInfo;
import kitra.awachat.next.dto.chat.ChatType;
import kitra.awachat.next.dto.chat.PrivateChatInfo;
import kitra.awachat.next.entity.PrivateChatEntity;
import kitra.awachat.next.entity.PrivateMessageAcknowledgeEntity;
import kitra.awachat.next.entity.PrivateMessageEntity;
import kitra.awachat.next.entity.UserEntity;
import kitra.awachat.next.mapper.PrivateChatMapper;
import kitra.awachat.next.mapper.PrivateMessageAcknowledgeMapper;
import kitra.awachat.next.mapper.PrivateMessageMapper;
import kitra.awachat.next.mapper.UserMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static kitra.awachat.next.util.DataBaseUtil.checkResult;

@Service
public class ChatService {

    private final UserMapper userMapper;
    private final PrivateChatMapper privateChatMapper;
    private final PrivateMessageMapper privateMessageMapper;
    private final PrivateMessageAcknowledgeMapper privateMessageAcknowledgeMapper;
    private final Logger logger = LogManager.getLogger(ChatService.class);

    public ChatService(UserMapper userMapper, PrivateChatMapper privateChatMapper, 
                      PrivateMessageMapper privateMessageMapper,
                      PrivateMessageAcknowledgeMapper privateMessageAcknowledgeMapper) {
        this.userMapper = userMapper;
        this.privateChatMapper = privateChatMapper;
        this.privateMessageMapper = privateMessageMapper;
        this.privateMessageAcknowledgeMapper = privateMessageAcknowledgeMapper;
    }

    public List<ChatInfo<PrivateChatInfo>> getChatList(Integer currentUserId) {
        // 创建会话列表
        List<ChatInfo<PrivateChatInfo>> chatList = new ArrayList<>();

        // 查询用户参与的所有私聊会话
        QueryWrapper<PrivateChatEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user1_id", currentUserId).or().eq("user2_id", currentUserId);
        List<PrivateChatEntity> privateChatEntities = privateChatMapper.selectList(queryWrapper);

        // 遍历私聊会话，获取对方用户信息
        for (PrivateChatEntity chatEntity : privateChatEntities) {
            // 确定对方用户ID
            Integer otherUserId = chatEntity.getUser1Id().equals(currentUserId) ? 
                                  chatEntity.getUser2Id() : chatEntity.getUser1Id();
            
            // 获取对方用户信息
            UserEntity otherUser = userMapper.selectById(otherUserId);
            if (otherUser == null) {
                logger.warn("找不到用户: {}", otherUserId);
                continue;
            }

            // 获取最后一条消息内容
            String lastMessageContent = null;
            if (chatEntity.getLastMessageId() != null) {
                PrivateMessageEntity lastMessage = privateMessageMapper.selectById(chatEntity.getLastMessageId());
                if (lastMessage != null) {
                    lastMessageContent = getMessageContent(lastMessage);
                }
            }

            // 计算未读消息数量
            Integer unreadCount = calculateUnreadCount(chatEntity.getChatId(), currentUserId);

            // 创建私聊信息
            PrivateChatInfo privateChatInfo = new PrivateChatInfo(
                chatEntity.getChatId(),
                otherUser.getUserId(),
                otherUser.getUsername(),
                otherUser.getNickname(),
                chatEntity.getCreatedAt(),
                chatEntity.getUpdatedAt(),
                lastMessageContent,
                unreadCount
            );

            // 创建聊天信息
            ChatInfo<PrivateChatInfo> chatInfo = new ChatInfo<>(ChatType.PRIVATE, privateChatInfo);

            // 添加到列表
            chatList.add(chatInfo);
        }

        return chatList;
    }

    /**
     * 获取消息内容
     * 
     * @param message 消息实体
     * @return 消息内容文本
     */
    private String getMessageContent(PrivateMessageEntity message) {
        try {
            // 根据消息类型处理内容
            if (message.getContentType() == 0) { // 文本消息
                Map<String, Object> content = message.getContent();
                if (content != null && content.containsKey("content")) {
                    return content.get("content").toString();
                }
            } else if (message.getContentType() == 1) { // 复合消息
                return "[复合消息]";
            }
        } catch (Exception e) {
            logger.error("解析消息内容失败", e);
        }
        return null;
    }

    /**
     * 计算未读消息数量
     * 
     * @param chatId 聊天ID
     * @param userId 用户ID
     * @return 未读消息数量
     */
    private Integer calculateUnreadCount(Long chatId, Integer userId) {
        // 查询用户已读的最后一条消息ID
        QueryWrapper<PrivateMessageAcknowledgeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chat_id", chatId).eq("user_id", userId);
        PrivateMessageAcknowledgeEntity ack = privateMessageAcknowledgeMapper.selectOne(queryWrapper);
        
        if (ack == null || ack.getLastMessageId() == null) {
            // 如果没有已读记录，查询该会话的所有消息数量
            QueryWrapper<PrivateMessageEntity> msgQuery = new QueryWrapper<>();
            msgQuery.eq("chat_id", chatId).eq("receiver_id", userId).eq("is_deleted", false);
            return privateMessageMapper.selectCount(msgQuery).intValue();
        } else {
            // 查询大于已读消息ID的消息数量
            QueryWrapper<PrivateMessageEntity> msgQuery = new QueryWrapper<>();
            msgQuery.eq("chat_id", chatId)
                   .eq("receiver_id", userId)
                   .gt("message_id", ack.getLastMessageId())
                   .eq("is_deleted", false);
            return privateMessageMapper.selectCount(msgQuery).intValue();
        }
    }

    /**
     * 创建或获取私聊会话
     *
     * @param user1Id 用户1ID
     * @param user2Id 用户2ID
     * @return 私聊会话实体
     */
    @Transactional
    public PrivateChatEntity createOrGetPrivateChat(Integer user1Id, Integer user2Id) {
        // 确保user1Id < user2Id，保持一致性
        if (user1Id > user2Id) {
            Integer temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }

        // 查询是否已存在会话
        QueryWrapper<PrivateChatEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user1_id", user1Id).eq("user2_id", user2Id);
        PrivateChatEntity chatEntity = privateChatMapper.selectOne(queryWrapper);

        // 如果不存在，创建新会话
        if (chatEntity == null) {
            chatEntity = new PrivateChatEntity();
            chatEntity.setUser1Id(user1Id);
            chatEntity.setUser2Id(user2Id);
            
            Date now = new Date();
            chatEntity.setCreatedAt(now);
            chatEntity.setUpdatedAt(now);
            
            // 插入数据库
            checkResult(privateChatMapper.insertPrivateChat(chatEntity));
        }

        return chatEntity;
    }
}