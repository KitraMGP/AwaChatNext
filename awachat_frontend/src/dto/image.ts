/**
 * 获取图片上传链接的请求参数
 */
export interface GetImageUploadUrlRequest {
  chatId: number
  fileSize: number
  mimeType: string
}

/**
 * 获取图片上传链接的响应数据
 */
export interface GetImageUploadUrlResponse {
  url: string
  key: string
}

/**
 * 获取图片下载链接的响应数据
 */
export interface GetImageDownloadUrlResponse {
  url: string
}
