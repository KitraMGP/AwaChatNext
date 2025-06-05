/**
 * WebSocket通讯的基础消息格式
 */
export interface WebSocketMessage<T> {
  type: string
  data: T | null
}

/**
 * 聊天消息的数据结构
 */
export interface ChatMessageData {
  msgType: string       // 消息类型：text, compound等
  conversationId: number // 聊天对话ID
  from: number          // 发送者ID
  to: number            // 接收者ID
  replyTo: number | null // 回复的消息ID
  content: unknown      // 消息内容，结构和msgType有关
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

// 聊天消息类型常量
export const CHAT_MSG_TYPE = {
  TEXT: 'text',
  COMPOUND: 'compound'
}

/**
 * 创建文本聊天消息
 */
export function createTextMessage(conversationId: number, from: number, to: number, text: string): ChatMessageData {
  return {
    msgType: CHAT_MSG_TYPE.TEXT,
    conversationId,
    from,
    to,
    replyTo: null,
    content: text
  }
}

/**
 * 创建回复文本消息
 */
export function createReplyTextMessage(conversationId: number, from: number, to: number, replyTo: number, text: string): ChatMessageData {
  return {
    msgType: CHAT_MSG_TYPE.TEXT,
    conversationId,
    from,
    to,
    replyTo,
    content: text
  }
}

/**
 * 创建复合消息
 */
export function createCompoundMessage(conversationId: number, from: number, to: number, content: unknown): ChatMessageData {
  return {
    msgType: CHAT_MSG_TYPE.COMPOUND,
    conversationId,
    from,
    to,
    replyTo: null,
    content
  }
}