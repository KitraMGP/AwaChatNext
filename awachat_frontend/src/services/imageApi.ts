import type { ApiResponse } from '@/dto/base'
import type { GetImageDownloadUrlResponse, GetImageUploadUrlRequest, GetImageUploadUrlResponse } from '@/dto/image'
import { api } from './api'
import type { AxiosResponse } from 'axios'

/**
 * 获取图片上传链接
 */
export const getImageUploadUrlApi = (request: GetImageUploadUrlRequest) =>
  api.post<GetImageUploadUrlRequest, AxiosResponse<ApiResponse<GetImageUploadUrlResponse>>>(
    '/image/getUploadUrl',
    request
  )

/**
 * 获取图片下载链接
 */
export const getImageDownloadUrlApi = (path: string) =>
  api.get<null, AxiosResponse<ApiResponse<GetImageDownloadUrlResponse>>>(
    `/image/getDownloadUrl?path=${encodeURIComponent(path)}`
  )