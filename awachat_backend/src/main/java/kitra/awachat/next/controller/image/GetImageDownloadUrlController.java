package kitra.awachat.next.controller.image;

import cn.dev33.satoken.stp.StpUtil;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.image.GetImageDownloadUrlResponse;
import kitra.awachat.next.service.MinioService;
import kitra.awachat.next.util.ApiUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取图片临时下载链接的接口
 */
@RequestMapping("/image")
@RestController
public class GetImageDownloadUrlController {
    private final MinioService minioService;

    public GetImageDownloadUrlController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/getDownloadUrl")
    public ApiResponse<GetImageDownloadUrlResponse> getImageDownloadUrl(@RequestParam String path) {
        String url = minioService.getImageDownloadURL(StpUtil.getLoginIdAsInt(), path);
        return ApiUtil.successfulResponse(new GetImageDownloadUrlResponse(url));
    }
}
