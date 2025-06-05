/**
 * 聊天类型枚举
 */
export type ChatType = 'private' | 'group'

/**
 * 聊天信息接口
 */
export interface ChatInfo<T> {
  type: ChatType
  info: T
}

/**
 * 私聊信息接口
 */
export interface PrivateChatInfo {
  chatId: number
  userId: number
  username: string
  nickname: string
  createdAt: string
  updatedAt: string
  lastMessageContent: string | null
  unreadCount: number
}

/**
 * 聊天列表响应接口
 */
export interface ChatListResponse {
  chats: ChatInfo<PrivateChatInfo>[]
}

/**
 * 用于显示在UI上的会话信息
 */
export interface DisplayConversation {
  id: number
  name: string
  lastMessage: string
  time: string
  unread: number
}
