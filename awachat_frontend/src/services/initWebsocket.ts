import { websocketService } from './websocketService'
import { ChatMessageType, MESSAGE_TYPE } from '@/dto/websocket'
import { showFailMessage, showSuccessfulMessage } from './api'
import type { ChatMessageData, FriendRequestMessageContent } from '@/dto/websocket'
import { useUserDataStore } from '@/stores/userDataStore'
import { ElNotification } from 'element-plus'

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

  // 根据消息类型进行不同处理
  switch (data.msgType) {
    case ChatMessageType.TEXT:
    case ChatMessageType.COMPOUND:
      // 处理普通聊天消息
      // 这里可以添加消息通知、更新聊天界面等逻辑
      break

    case ChatMessageType.FRIEND_REQUEST:
      // 处理好友请求消息
      handleFriendRequestMessage(data as ChatMessageData<FriendRequestMessageContent>)
      break

    default:
      console.warn('未知的消息类型:', data.msgType)
  }
}

/**
 * 处理好友请求消息
 * @param data 好友请求消息数据
 */
function handleFriendRequestMessage(data: ChatMessageData<FriendRequestMessageContent>): void {
  console.log('收到好友请求:', data)

  // 这里可以添加显示好友请求通知、更新好友请求列表等逻辑
  // 例如：显示一个通知，让用户点击接受或拒绝

  // 如果是自己发出的好友请求，则不显示通知
  const userData = useUserDataStore()
  const currentUserId = userData.value?.userId // 假设有一个获取当前用户ID的函数
  if (data.from === currentUserId) {
    return
  }

  // 显示好友请求通知
  ElNotification({
    title: '新的好友请求',
    message: `用户 ${data.from} 请求添加您为好友`,
    type: 'info'
  })
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
