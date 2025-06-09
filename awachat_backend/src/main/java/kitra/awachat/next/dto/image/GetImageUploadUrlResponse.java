package kitra.awachat.next.dto.image;

/**
 * 获取图片上传链接接口的返回数据
 * @param url 图片上传URL
 * @param key 图片在 MinIO 中的存储路径（用于附加到聊天消息中）
 */
public record GetImageUploadUrlResponse(String url, String key) {
}
