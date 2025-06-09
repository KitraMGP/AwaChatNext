package kitra.awachat.next.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import kitra.awachat.next.dto.image.GetImageUploadUrlResponse;
import kitra.awachat.next.entity.PrivateChatEntity;
import kitra.awachat.next.exception.*;
import kitra.awachat.next.mapper.PrivateChatMapper;
import kitra.awachat.next.mapper.PrivateMessageMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 负责处理图片上传下载相关逻辑（生成MinIO图片上传链接等等）
 */
@Service
public class MinioService {
    private final PrivateChatMapper privateChatMapper;
    private final PrivateMessageMapper privateMessageMapper; // 添加消息Mapper
    private final MinioClient minioClient;
    private final Logger logger = LogManager.getLogger(MinioService.class);
    @Value("${minio.bucket-name}")
    private String bucketName;
    @Value("${minio.image-size-limit}")
    private Long imageSizeLimit;
    @Value("${minio.url}")
    private String localUrl;
    @Value("${minio.public-url}")
    private String publicUrl;

    public MinioService(PrivateChatMapper privateChatMapper, PrivateMessageMapper privateMessageMapper, MinioClient minioClient) {
        this.privateChatMapper = privateChatMapper;
        this.privateMessageMapper = privateMessageMapper;
        this.minioClient = minioClient;
    }

    /**
     * 检查文件大小和文件类型，检查是否有权限，并生成图片上传链接
     *
     * @param chatId   聊天ID
     * @param userId   上传图片的用户ID
     * @param fileSize 要上传的文件大小
     * @param mimeType 文件MIME类型
     * @return 包含上传URL和图片存储路径的响应数据
     */
    public GetImageUploadUrlResponse getImageUploadUrlResponse(Long chatId, Integer userId, Long fileSize, String mimeType) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 检查文件大小是否超限
        if (fileSize > imageSizeLimit) {
            throw new FileSizeExceededLimitException(imageSizeLimit + "字节");
        }
        // 检查文件类型是否为图片
        if (!mimeType.startsWith("image/")) {
            throw new IllegalFileFormatException();
        }
        // 检查会话是否存在
        QueryWrapper<PrivateChatEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chat_id", chatId);
        PrivateChatEntity privateChatEntity = privateChatMapper.selectOne(queryWrapper);
        if (privateChatEntity == null) {
            throw new NoSuchChatException();
        }
        // 检查用户是否在会话中
        if (!privateChatEntity.getUser1Id().equals(userId) && !privateChatEntity.getUser2Id().equals(userId)) {
            throw new PermissionDeniedException();
        }
        // 生成路径并获取URL
        String path = "chat/" + chatId + "/user_" + userId + "/" + UUID.randomUUID() + ".jpg";
        String uploadUrl = minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder().method(Method.PUT).bucket(bucketName).object(path).expiry(5, TimeUnit.MINUTES).build()
        );
        uploadUrl = uploadUrl.replace(localUrl, publicUrl); // 替换为公网地址
        logger.info("用户{}请求生成了图片上传URL，聊天ID={}，文件大小={}，文件类型={}，存储路径={}", userId, chatId, fileSize, mimeType, path);
        return new GetImageUploadUrlResponse(uploadUrl, path);
    }

    /**
     * 根据路径生成图片的临时下载链接
     *
     * @param userId 发出请求的用户ID（用于验证权限）
     * @param path   图片存储路径
     * @return 下载链接
     */
    public String getImageDownloadURL(Integer userId, String path) {
        // 示例路径：chat/15/user_5/xxx.jpg
        String[] parts = path.split("/");
        // 首先检查基本结构
        if (parts.length != 4) {
            throw new BadInputException("图片路径");
        }
        // 解析chatId
        long chatId;
        try {
            chatId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new BadInputException("图片路径");
        }
        // 检查会话是否存在
        QueryWrapper<PrivateChatEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chat_id", chatId);
        PrivateChatEntity privateChatEntity = privateChatMapper.selectOne(queryWrapper);
        if (privateChatEntity == null) {
            throw new NoSuchChatException();
        }
        // 检查用户是否在会话中
        if (!privateChatEntity.getUser1Id().equals(userId) && !privateChatEntity.getUser2Id().equals(userId)) {
            throw new PermissionDeniedException();
        }
        // 生成URL
        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(path).expiry(5, TimeUnit.MINUTES).build()
            );
            return url.replace(localUrl, publicUrl); // 替换为公网地址
        } catch (ServerException | InvalidResponseException | InsufficientDataException | ErrorResponseException |
                 IOException | NoSuchAlgorithmException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new BadInputException("路径不存在");
        }

    }
}
