import { websocketService } from './websocketService'
import { MESSAGE_TYPE } from '@/dto/websocket'
import { showFailMessage, showSuccessfulMessage } from './api'
import type { ChatMessageData } from '@/dto/websocket'

/**
 * 初始化WebSocket服务
 * 在应用启动时调用此函数
 */
export function initWebSocketService(): void {
  // 使用相对路径，通过vite代理访问WebSocket服务
  // 不再指定具体的主机名和端口号，而是使用相对路径
  const baseUrl = window.location.origin

  // 初始化WebSocket连接
  websocketService.init(baseUrl)

  // 注册消息处理器
  registerMessageHandlers()
}

/**
 * 注册各类消息的处理函数
 */
function registerMessageHandlers(): void {
  // 注册聊天消息处理器
  websocketService.registerMessageHandler(MESSAGE_TYPE.CHAT, handleChatMessage)

  // 注册系统消息处理器
  websocketService.registerMessageHandler(MESSAGE_TYPE.SYSTEM, handleSystemMessage)

  // 注册错误消息处理器
  websocketService.registerMessageHandler(MESSAGE_TYPE.ERROR, handleErrorMessage)
}

/**
 * 处理聊天消息
 * @param data 聊天消息数据
 */
function handleChatMessage(data: ChatMessageData<unknown>): void {
  console.log('收到聊天消息:', data)
  // 这里可以添加消息通知、更新聊天界面等逻辑
  // 例如：将消息添加到聊天记录、更新未读消息计数等
}

/**
 * 处理系统消息
 * @param data 系统消息数据
 */
function handleSystemMessage(data: string): void {
  console.log('收到系统消息:', data)
  showSuccessfulMessage(`系统消息: ${data}`)
}

/**
 * 处理错误消息
 * @param data 错误消息数据
 */
function handleErrorMessage(data: string): void {
  console.error('收到错误消息:', data)
  showFailMessage('错误', data)
}
