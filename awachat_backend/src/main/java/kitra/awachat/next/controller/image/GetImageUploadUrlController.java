package kitra.awachat.next.controller.image;

import cn.dev33.satoken.stp.StpUtil;
import io.minio.errors.*;
import jakarta.validation.Valid;
import kitra.awachat.next.dto.ApiResponse;
import kitra.awachat.next.dto.image.GetImageUploadUrlRequest;
import kitra.awachat.next.dto.image.GetImageUploadUrlResponse;
import kitra.awachat.next.service.MinioService;
import kitra.awachat.next.util.ApiUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RequestMapping("/image")
@RestController
public class GetImageUploadUrlController {
    private final MinioService minioService;

    public GetImageUploadUrlController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/getUploadUrl")
    public ApiResponse<GetImageUploadUrlResponse> getImageUploadUrl(@RequestBody @Valid GetImageUploadUrlRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetImageUploadUrlResponse response = minioService.getImageUploadUrlResponse(request.chatId(), StpUtil.getLoginIdAsInt(), request.fileSize(), request.mimeType());
        return ApiUtil.successfulResponse(response);
    }
}
