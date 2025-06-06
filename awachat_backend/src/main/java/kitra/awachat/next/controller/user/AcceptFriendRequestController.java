package kitra.awachat.next.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.user.AcceptFriendRequestRequest;
import kitra.awachat.next.dto.websocket.ChatMessageData;
import kitra.awachat.next.dto.websocket.ChatMessageType;
import kitra.awachat.next.dto.websocket.ChatType;
import kitra.awachat.next.dto.websocket.TextMessageContent;
import kitra.awachat.next.entity.PrivateChatEntity;
import kitra.awachat.next.exception.FriendRequestNotFoundException;
import kitra.awachat.next.exception.UserNotFoundException;
import kitra.awachat.next.service.ChatMessageService;
import kitra.awachat.next.service.ChatService;
import kitra.awachat.next.service.FriendService;
import kitra.awachat.next.service.UserService;
import kitra.awachat.next.session.WebSocketSessionManager;
import kitra.awachat.next.util.ApiUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequestMapping("/user")
@RestController
public class AcceptFriendRequestController {

    private final UserService userService;
    private final FriendService friendService;
    private final WebSocketSessionManager webSocketSessionManager;
    private final ChatService chatService;
    private final ChatMessageService chatMessageService;

    private final Logger logger = LogManager.getLogger(AcceptFriendRequestController.class);

    public AcceptFriendRequestController(UserService userService, FriendService friendService, WebSocketSessionManager webSocketSessionManager, ChatService chatService, ChatMessageService chatMessageService) {
        this.userService = userService;
        this.friendService = friendService;
        this.webSocketSessionManager = webSocketSessionManager;
        this.chatService = chatService;
        this.chatMessageService = chatMessageService;
    }

    @PostMapping("/acceptFriendRequest")
    public ApiResponse<Void> acceptFriendRequest(@Valid @RequestBody AcceptFriendRequestRequest acceptFriendRequestRequest) {
        // 获取当前登录用户ID
        Integer currentUserId = StpUtil.getLoginIdAsInt();

        // 获取请求中的发起用户ID
        Integer originUserId = acceptFriendRequestRequest.originUserId();

        // 检查发起用户是否存在
        if (userService.getUserById(originUserId) == null) {
            throw new UserNotFoundException();
        }

        // 检查是否已经是好友
        if (friendService.areFriends(currentUserId, originUserId)) {
            // 如果已经是好友，直接返回成功
            return ApiUtil.successfulResponse(null);
        }

        // 查找并更新好友请求消息
        boolean requestFound = friendService.acceptFriendRequest(originUserId, currentUserId);

        if (!requestFound) {
            throw new FriendRequestNotFoundException();
        }

        // 添加好友关系
        boolean addFriendSuccess = friendService.addFriend(currentUserId, originUserId);
        if (!addFriendSuccess) {
            logger.warn("接受好友请求失败：无法建立好友关系，接受者：{}，申请者：{}", currentUserId, originUserId);
            return ApiUtil.failedResponse(200499, "添加好友失败");
        }
        // 发送欢迎消息
        // TODO 这里存在优化空间，代码和ChatWebSocketHandler中发送消息的代码有部分重复，考虑将发送消息的代码封装？
        webSocketSessionManager.getSessionsByUser(originUserId).forEach(session -> {
            // 1. 创建或获取私聊会话
            PrivateChatEntity chatEntity = chatService.createOrGetPrivateChat(currentUserId, originUserId);
            // 2. chatId填入消息中
            ChatMessageData<TextMessageContent> chatMessageData = new ChatMessageData<>(
                0L, // 目前还没有生成消息ID
                ChatType.PRIVATE,
                ChatMessageType.TEXT,
                chatEntity.getChatId(),  // 使用新的chatId
                currentUserId,
                originUserId,
                null,
                new TextMessageContent("我们已成为好友，现在开始聊天吧！"),
                new Date()
            );
            // 3. 处理聊天消息
            boolean handleSuccess = chatMessageService.handleChatMessage(currentUserId, chatMessageData);
            if (!handleSuccess) {
                logger.warn("未能发送加好友成功欢迎消息。接受好友请求者：{}，申请者：{}", currentUserId, originUserId);
            }
        });
        return ApiUtil.successfulResponse(null);
    }
}
