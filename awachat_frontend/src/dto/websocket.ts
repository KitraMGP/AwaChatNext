/**
 * WebSocket通讯的基础消息格式
 */
export interface WebSocketMessage<T> {
  type: string
  data: T | null
}

/**
 * 聊天类型
 */
export type ChatType = 'private' | 'group'

// ChatType常量
export const ChatType = {
  PRIVATE: 'private' as ChatType,
  GROUP: 'group' as ChatType,
}

/**
 * 消息类型
 */
export type ChatMessageType = 'text' | 'compound' | 'friend_request'

// ChatMessageType常量
export const ChatMessageType = {
  TEXT: 'text' as ChatMessageType,
  COMPOUND: 'compound' as ChatMessageType,
  FRIEND_REQUEST: 'friend_request' as ChatMessageType,
}

/**
 * 好友请求消息内容
 */
export interface FriendRequestMessageContent {
  isAccepted: boolean
}

/**
 * 聊天消息数据结构
 */
export interface ChatMessageData<
  T = TextMessageContent | CompoundMessageContent | FriendRequestMessageContent,
> {
  id?: number // 消息ID（可选）
  chatType: ChatType // 聊天类型：私聊或群聊
  msgType: ChatMessageType // 消息类型：文本、复合或好友请求
  chatId: number // 聊天ID
  from: number // 发送者ID
  to: number // 接收者ID
  replyTo?: number // 回复的消息ID（可选）
  content: T // 消息内容，结构和msgType有关
  sentAt?: string // 发送时间（可选，ISO格式字符串）
}

/**
 * 生成 ChatMessageData 的文字预览。用于消息列表实时更新
 */
export function previewChatMessageData(data: ChatMessageData): string {
  switch (data.msgType) {
    case 'text':
      return (data.content as TextMessageContent).content
    case 'compound':
      return '[图文消息]'
    case 'friend_request':
      return '[好友请求]'
    default:
      return '[未知消息类型]'
  }
}

/**
 * 创建好友请求消息
 */
export function createFriendRequestMessage(
  chatId: number,
  from: number,
  to: number,
  isAccepted: boolean = false,
): ChatMessageData<FriendRequestMessageContent> {
  return {
    chatType: ChatType.PRIVATE,
    msgType: ChatMessageType.FRIEND_REQUEST,
    chatId,
    from,
    to,
    content: { isAccepted },
  }
}

/**
 * 复合消息部分类型
 */
export type CompoundMessagePartType = 'text' | 'image'

// CompoundMessagePartType常量
export const CompoundMessagePartType = {
  TEXT: 'text' as CompoundMessagePartType,
  IMAGE: 'image' as CompoundMessagePartType,
}

/**
 * 文本消息内容
 */
export interface TextMessageContent {
  content: string
}

/**
 * 复合消息部分
 */
export interface CompoundMessagePart {
  type: CompoundMessagePartType
  content: string
}

/**
 * 复合消息内容
 */
export interface CompoundMessageContent {
  parts: CompoundMessagePart[]
}

/**
 * 心跳消息的数据结构
 */
export interface HeartbeatData {
  timestamp: number
}

// 消息类型常量
export const MESSAGE_TYPE = {
  CHAT: 'chat',
  HEARTBEAT: 'heartbeat',
  ERROR: 'error',
  SYSTEM: 'system',
  ACK: 'ack', // 仅客户端发送
  REQUEST_CHAT_HISTORY: 'request_chat_history', // 仅客户端发送
}

/**
 * 创建文本聊天消息
 */
export function createTextMessage(
  chatId: number,
  from: number,
  to: number,
  text: string,
): ChatMessageData<TextMessageContent> {
  return {
    chatType: ChatType.PRIVATE,
    msgType: ChatMessageType.TEXT,
    chatId,
    from,
    to,
    content: { content: text },
  }
}

/**
 * 创建复合消息
 */
export function createCompoundMessage(
  chatId: number,
  from: number,
  to: number,
  parts: CompoundMessagePart[],
): ChatMessageData<CompoundMessageContent> {
  return {
    chatType: ChatType.PRIVATE,
    msgType: ChatMessageType.COMPOUND,
    chatId,
    from,
    to,
    content: { parts },
  }
}

// 聊天消息类型常量
export const CHAT_MSG_TYPE = {
  TEXT: 'text',
  COMPOUND: 'compound',
}

/**
 * 创建回复文本消息
 */
export function createReplyTextMessage(
  chatType: ChatType,
  chatId: number,
  from: number,
  to: number,
  replyTo: number,
  text: string,
): ChatMessageData {
  return {
    chatType: chatType,
    msgType: ChatMessageType.TEXT,
    chatId,
    from,
    to,
    replyTo: replyTo,
    content: {
      content: text,
    },
  }
}

/**
 * 请求聊天历史消息数据结构
 */
export interface RequestChatHistoryData {
  chatType: ChatType
  chatId: number
  lastMessageId?: number
}

/**
 * 客户端接收服务端发送的历史消息数据结构
 */
export interface ChatHistoryData {
  history: ChatMessageData[]
}

export interface ReadAcknowledgeData {
  chatType: ChatType
  chatId: number
  lastMessageId: number
}
