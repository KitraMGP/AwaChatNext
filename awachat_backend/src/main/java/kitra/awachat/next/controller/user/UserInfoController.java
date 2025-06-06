package kitra.awachat.next.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.user.UserDataResponse;
import kitra.awachat.next.entity.UserEntity;
import kitra.awachat.next.exception.UserNotFoundException;
import kitra.awachat.next.service.FriendService;
import kitra.awachat.next.service.UserService;
import kitra.awachat.next.util.ApiUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息相关接口
 */
@RequestMapping("/user")
@RestController
public class UserInfoController {
    private final UserService userService;
    private final FriendService friendService;

    public UserInfoController(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
    }

    /**
     * 获取用户信息
     *
     * @param userId 可选的用户ID参数，如果不提供则返回当前登录用户的信息
     * @return 用户信息
     */
    @GetMapping("/info")
    public ApiResponse<UserDataResponse> getUserInfo(
        @RequestParam(required = false) Integer userId,
        @RequestParam(required = false) String username) {
        // 如果没有提供userId和username，则获取当前登录用户的ID
        int currentUserId = StpUtil.getLoginIdAsInt();
        UserEntity userEntity;

        if (userId != null) {
            // 通过ID查询用户
            userEntity = userService.getUserById(userId);
        } else if (username != null && !username.isEmpty()) {
            // 通过用户名查询用户
            userEntity = userService.getUserByUsername(username);
            if (userEntity == null) {
                throw new UserNotFoundException();
            }
            userId = userEntity.getUserId();
        } else {
            // 获取当前用户
            userId = currentUserId;
            userEntity = userService.getUserById(userId);
        }

        // 检查是否为好友（如果查询的不是自己）
        Boolean isFriend = false;
        if (!userId.equals(currentUserId)) {
            isFriend = friendService.areFriends(currentUserId, userId);
        }

        // 构建返回数据
        UserDataResponse userData = new UserDataResponse(
            userEntity.getUserId(),
            userEntity.getUsername(),
            userEntity.getNickname(),
            userEntity.getDescription(),
            userEntity.getAvatar(),
            userEntity.getCreatedAt(),
            userEntity.getLastOnlineAt(),
            userEntity.getRole(),
            userEntity.getBanUntil(),
            isFriend, // 设置是否为好友
            userEntity.getExtendedData()
        );

        return ApiUtil.successfulResponse(userData);
    }
}