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
  GROUP: 'group' as ChatType
}

/**
 * 消息类型
 */
export type ChatMessageType = 'text' | 'compound'

// ChatMessageType常量
export const ChatMessageType = {
  TEXT: 'text' as ChatMessageType,
  COMPOUND: 'compound' as ChatMessageType
}

/**
 * 复合消息部分类型
 */
export type CompoundMessagePartType = 'text' | 'image'

// CompoundMessagePartType常量
export const CompoundMessagePartType = {
  TEXT: 'text' as CompoundMessagePartType,
  IMAGE: 'image' as CompoundMessagePartType
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
 * 聊天消息的数据结构
 */
export interface ChatMessageData<T = TextMessageContent | CompoundMessageContent> {
  id?: number            // 消息ID（可选）
  chatType: ChatType       // 聊天类型：私聊或群聊
  msgType: ChatMessageType // 消息类型：文本或复合
  chatId: number           // 聊天ID
  from: number             // 发送者ID
  to: number               // 接收者ID
  replyTo?: number         // 回复的消息ID（可选）
  content: T               // 消息内容，结构和msgType有关
  sentAt?: string          // 发送时间（可选，ISO格式字符串）
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
  SYSTEM: 'system'
}

/**
 * 创建文本聊天消息
 */
export function createTextMessage(chatId: number, from: number, to: number, text: string): ChatMessageData<TextMessageContent> {
  return {
    chatType: ChatType.PRIVATE,
    msgType: ChatMessageType.TEXT,
    chatId,
    from,
    to,
    content: { content: text }
  }
}

/**
 * 创建复合消息
 */
export function createCompoundMessage(chatId: number, from: number, to: number, parts: CompoundMessagePart[]): ChatMessageData<CompoundMessageContent> {
  return {
    chatType: ChatType.PRIVATE,
    msgType: ChatMessageType.COMPOUND,
    chatId,
    from,
    to,
    content: { parts }
  }
}

// 聊天消息类型常量
export const CHAT_MSG_TYPE = {
  TEXT: 'text',
  COMPOUND: 'compound'
}

/**
 * 创建回复文本消息
 */
export function createReplyTextMessage(chatType: ChatType, chatId: number, from: number, to: number, replyTo: number, text: string): ChatMessageData {
  return {
    chatType: chatType,
    msgType: ChatMessageType.TEXT,
    chatId,
    from,
    to,
    replyTo: replyTo,
    content: {
      content: text
    }
  }
}
