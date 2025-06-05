package kitra.awachat.next.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.user.UserDataResponse;
import kitra.awachat.next.entity.UserEntity;
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

    public UserInfoController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户信息
     *
     * @param userId 可选的用户ID参数，如果不提供则返回当前登录用户的信息
     * @return 用户信息
     */
    @GetMapping("/info")
    public ApiResponse<UserDataResponse> getUserInfo(@RequestParam(required = false) Integer userId) {
        // 如果没有提供userId，则获取当前登录用户的ID
        if (userId == null) {
            userId = StpUtil.getLoginIdAsInt();
        }

        // 获取用户信息
        UserEntity userEntity = userService.getUserById(userId);

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
            userEntity.getExtendedData()
        );

        return ApiUtil.successfulResponse(userData);
    }
}