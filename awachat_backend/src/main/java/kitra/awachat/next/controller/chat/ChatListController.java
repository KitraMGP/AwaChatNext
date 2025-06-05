package kitra.awachat.next.controller.chat;

import cn.dev33.satoken.stp.StpUtil;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.chat.ChatInfo;
import kitra.awachat.next.dto.chat.ChatListResponse;
import kitra.awachat.next.dto.chat.PrivateChatInfo;
import kitra.awachat.next.service.ChatService;
import kitra.awachat.next.util.ApiUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/chat")
@RestController
public class ChatListController {
    private final ChatService chatService;

    public ChatListController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/list")
    public ApiResponse<ChatListResponse> getChatList() {
        // 获取当前登录用户ID
        Integer currentUserId = StpUtil.getLoginIdAsInt();

        // 获取会话列表
        List<ChatInfo<PrivateChatInfo>> chatList = chatService.getChatList(currentUserId);

        // 转换为数组
        ChatInfo<?>[] chatInfoArray = chatList.toArray(new ChatInfo<?>[0]);

        // 创建响应
        ChatListResponse response = new ChatListResponse(chatInfoArray);

        return ApiUtil.successfulResponse(response);
    }
}
