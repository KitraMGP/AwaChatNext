import type { ApiResponse } from '@/dto/base'
import type { ChatListResponse } from '@/dto/chat'
import { api } from './api'

/**
 * 获取聊天列表
 */
export const chatListApi = () => api.get<ApiResponse<ChatListResponse>>('/chat/list')