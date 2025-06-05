package kitra.awachat.next.controller;

import cn.dev33.satoken.exception.NotLoginException;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.exception.ApiException;
import kitra.awachat.next.exception.DatabaseOperationException;
import kitra.awachat.next.util.ApiUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@RestController
public class GlobalExceptionHandler {
    private final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * 自定义API异常的处理器
     */
    @ExceptionHandler(ApiException.class)
    public ApiResponse<Void> handleApiException(ApiException e) {
        return ApiUtil.failedResponse(e.getCode(), e.getMessage());
    }

    /**
     * 未登录异常处理器
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLogin(NotLoginException e) {
        return ApiUtil.unauthorizedResponse();
    }

    /**
     * 数据库访问异常的处理器
     */
    @ExceptionHandler(DatabaseOperationException.class)
    public ApiResponse<Void> handleDatabaseOperationException(DatabaseOperationException e) {
        logger.error("数据库访问异常！", e);
        return ApiUtil.failedResponse(e.getCode(), e.getMessage());
    }

    /**
     * 其他 HTTP 请求错误
     */
    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        MissingServletRequestPartException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class
    })
    public ApiResponse<Void> handleMethodArgumentTypeMismatchException() {
        return ApiUtil.failedResponse(200409, "请求格式有误");
    }

    /**
     * 上传文件大小超出application.yml设置值引发的异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<Void> handleMaxUploadSizeExceededException() {
        return ApiUtil.failedResponse(200406, "文件长度超出限制");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        return ApiUtil.failedResponse(200410, "接口不存在：" + e.getResourcePath());
    }

    /**
     * 其他异常的处理器
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        logger.error("系统发生异常：", e);
        return ApiUtil.failedResponse(200500, "系统繁忙，请稍后再试");
    }
}
