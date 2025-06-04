package kitra.awachat.next.util;

import kitra.awachat.next.dto.ApiResponse;
import org.springframework.http.HttpStatus;

public class ApiUtil {
    /**
     * 用于快速生成“用户未登录”的返回数据
     *
     * @return ResponseEntity，可直接作为 Controller 方法中的返回值
     */
    public static <T> ApiResponse<T> unauthorizedResponse() {
        return new ApiResponse<>(200401, null, "用户未登录");
    }

    /**
     * 用于快速生成“成功”的返回数据
     *
     * @param data 返回数据结构中附带的data
     * @return ApiResponse，可直接作为Controller方法中的返回值
     */
    public static <T> ApiResponse<T> successfulResponse(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), data, "成功");
    }

    /**
     * 用来快速生成“失败”的返回数据
     *
     * @param code    API返回数据中的code
     * @param message API返回数据中的message
     * @return ApiResponse，可直接作为Controller方法中的返回值
     */
    public static <T> ApiResponse<T> failedResponse(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    public static <T> ApiResponse<T> databaseFailureResponse() {
        return failedResponse(200, "数据库操作失败");
    }
}
