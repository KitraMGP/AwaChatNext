package kitra.awachat.next.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.user.DeleteFriendRequest;
import kitra.awachat.next.exception.NotFriendsException;
import kitra.awachat.next.service.FriendService;
import kitra.awachat.next.util.ApiUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 删除好友接口
 */
@RequestMapping("/user")
@RestController
public class DeleteFriendController {
    private final FriendService friendService;

    public DeleteFriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/deleteFriend")
    public ApiResponse<Void> deleteFriend(@RequestBody @Valid DeleteFriendRequest request) {
        Integer currentUserId = StpUtil.getLoginIdAsInt();
        Integer userToDelete = request.userId();
        if (friendService.deleteFriend(currentUserId, userToDelete)) {
            return ApiUtil.successfulResponse(null);
        } else {
            throw new NotFriendsException();
        }
    }
}
