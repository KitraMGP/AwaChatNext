package kitra.awachat.next.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.user.LoginRequest;
import kitra.awachat.next.dto.user.LoginResponse;
import kitra.awachat.next.dto.user.LogoutResponse;
import kitra.awachat.next.dto.user.UserDataResponse;
import kitra.awachat.next.entity.UserEntity;
import kitra.awachat.next.service.UserService;
import kitra.awachat.next.util.ApiUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 包含了和用户权限认证有关的接口，如注册、退出登录。这些接口均会改变客户端的登录状态，返回的数据中包含了 Token 字段
 */
@RequestMapping("/user")
@RestController
public class UserAuthenticationController {
    private final UserService userService;

    public UserAuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        UserEntity userEntity = userService.login(loginRequest.username(), loginRequest.password());
        StpUtil.login(userEntity.getUserId());
        UserDataResponse userData = new UserDataResponse(userEntity.getUserId(), userEntity.getUsername(), userEntity.getNickname(), userEntity.getDescription(), userEntity.getAvatar(), userEntity.getCreatedAt(), userEntity.getLastOnlineAt(), userEntity.getRole(), userEntity.getBanUntil(), false, userEntity.getExtendedData());
        return ApiUtil.successfulResponse(new LoginResponse(userData, StpUtil.getTokenValue()));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
            return ApiUtil.successfulResponse(new LogoutResponse(StpUtil.getTokenValue()));
        } else {
            return ApiUtil.unauthorizedResponse();
        }
    }
}
