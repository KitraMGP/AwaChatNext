package kitra.awachat.next.controller.user;

import jakarta.validation.Valid;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.user.RegisterRequest;
import kitra.awachat.next.service.UserService;
import kitra.awachat.next.util.ApiUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口
 */
@RequestMapping("/user")
@RestController
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request.username(), request.nickname(), request.password());
        return ApiUtil.successfulResponse(null);
    }
}
