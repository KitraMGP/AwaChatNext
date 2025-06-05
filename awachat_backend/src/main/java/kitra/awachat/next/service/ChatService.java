package kitra.awachat.next.service;

import kitra.awachat.next.dto.chat.ChatInfo;
import kitra.awachat.next.dto.chat.ChatType;
import kitra.awachat.next.dto.chat.PrivateChatInfo;
import kitra.awachat.next.entity.UserEntity;
import kitra.awachat.next.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ChatService {

    private final UserMapper userMapper;

    public ChatService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<ChatInfo<PrivateChatInfo>> getChatList(Integer currentUserId) {
        // 获取所有用户
        List<UserEntity> allUsers = userMapper.selectList(null);

        // 创建会话列表
        List<ChatInfo<PrivateChatInfo>> chatList = new ArrayList<>();

        // 当前时间
        Date now = new Date();

        // 遍历所有用户，排除当前用户自己
        for (UserEntity user : allUsers) {
            if (user.getUserId() != currentUserId) {
                // 创建私聊信息，直接存储对方用户的基本信息
                PrivateChatInfo privateChatInfo = new PrivateChatInfo(
                    0L, // chatId设为0
                    user.getUserId(), // 对方用户ID
                    user.getUsername(), // 对方用户名
                    user.getNickname(), // 对方昵称
                    now, // createdAt为当前时间
                    now, // updatedAt为当前时间
                    null // lastMessageId为null
                );

                // 创建聊天信息
                ChatInfo<PrivateChatInfo> chatInfo = new ChatInfo<>(ChatType.PRIVATE, privateChatInfo);

                // 添加到列表
                chatList.add(chatInfo);
            }
        }

        return chatList;
    }
}