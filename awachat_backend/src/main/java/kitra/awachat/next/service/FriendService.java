package kitra.awachat.next.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import kitra.awachat.next.entity.FriendEntity;
import kitra.awachat.next.entity.PrivateMessageEntity;
import kitra.awachat.next.mapper.FriendMapper;
import kitra.awachat.next.mapper.PrivateMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class FriendService {

    private final FriendMapper friendMapper;
    private final PrivateMessageMapper privateMessageMapper;

    public FriendService(FriendMapper friendMapper, PrivateMessageMapper privateMessageMapper) {
        this.friendMapper = friendMapper;
        this.privateMessageMapper = privateMessageMapper;
    }

    /**
     * 检查两个用户是否是好友
     *
     * @param user1Id 用户1ID
     * @param user2Id 用户2ID
     * @return 如果是好友返回true，否则返回false
     */
    public boolean areFriends(Integer user1Id, Integer user2Id) {
        // 确保user1Id < user2Id，保持一致性
        if (user1Id > user2Id) {
            Integer temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }

        // 查询好友关系
        QueryWrapper<FriendEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user1", user1Id).eq("user2", user2Id);
        return friendMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 添加好友关系
     *
     * @param user1Id 用户1ID
     * @param user2Id 用户2ID
     * @return 添加成功返回true，否则返回false
     */
    public boolean addFriend(Integer user1Id, Integer user2Id) {
        // 确保user1Id < user2Id，保持一致性
        if (user1Id > user2Id) {
            Integer temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }

        // 检查是否已经是好友
        if (areFriends(user1Id, user2Id)) {
            return true;
        }

        // 创建好友关系
        FriendEntity friendEntity = new FriendEntity();
        friendEntity.setUser1(user1Id);
        friendEntity.setUser2(user2Id);

        return friendMapper.insert(friendEntity) > 0;
    }

    /**
     * 接受好友请求
     *
     * @param fromUserId 发起请求的用户ID
     * @param toUserId   接收请求的用户ID
     * @return 是否找到并更新了好友请求
     */
    @Transactional
    public boolean acceptFriendRequest(Integer fromUserId, Integer toUserId) {
        // 查找好友请求消息
        QueryWrapper<PrivateMessageEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sender_id", fromUserId)
            .eq("receiver_id", toUserId)
            .eq("content_type", 2) // 好友请求消息类型
            .orderByDesc("sent_at");

        List<PrivateMessageEntity> requestMessages = privateMessageMapper.selectList(queryWrapper);

        if (requestMessages.isEmpty()) {
            return false;
        }
        requestMessages.forEach(requestMessage -> {
            // 更新消息内容，设置isAccepted为true
            Map<String, Object> content = requestMessage.getContent();
            content.put("isAccepted", true);
            requestMessage.setContent(content);

            // 更新消息
            privateMessageMapper.updateById(requestMessage);
        });
        return true;
    }

    public boolean deleteFriend(Integer user1Id, Integer user2Id) {
        // 确保user1Id < user2Id，保持一致性
        if (user1Id > user2Id) {
            Integer temp = user1Id;
            user1Id = user2Id;
            user2Id = temp;
        }
        QueryWrapper<FriendEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user1", user1Id).eq("user2", user2Id);
        return friendMapper.delete(queryWrapper) > 0;
    }
}