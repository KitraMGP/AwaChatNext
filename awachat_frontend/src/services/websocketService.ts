import { ref } from 'vue'
import type { ChatMessageData, HeartbeatData, WebSocketMessage } from '@/dto/websocket'
import { MESSAGE_TYPE } from '@/dto/websocket'
import { showFailMessage } from './api'

/**
 * WebSocket服务类
 * 用于管理与后端的WebSocket连接、消息发送和接收
 */
class WebSocketService {
  private socket: WebSocket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 3000 // 重连间隔，单位毫秒
  private heartbeatInterval: number | null = null
  private url = ''
  
  // 连接状态
  public connected = ref(false)
  // 最后一次接收到的消息
  public lastMessage = ref<any>(null)
  // 消息处理器映射
  private messageHandlers: Map<string, (data: any) => void> = new Map()

  /**
   * 初始化WebSocket连接
   * @param baseUrl 基础URL，例如：http://localhost:3000
   */
  public init(baseUrl: string): void {
    // 构建WebSocket URL，使用相对路径通过vite代理访问
    // 将http://localhost:3000 转换为 ws://localhost:3000/api/ws/chat
    const wsProtocol = baseUrl.startsWith('https') ? 'wss' : 'ws'
    const wsBaseUrl = baseUrl.replace(/^https?/, wsProtocol)
    this.url = `${wsBaseUrl}/api/ws/chat`
    this.connect()
  }

  /**
   * 建立WebSocket连接
   */
  private connect(): void {
    if (this.socket) {
      this.socket.close()
    }

    try {
      // 获取token
      const token = localStorage.getItem('satoken')
      if (!token) {
        console.error('未找到认证token，无法建立WebSocket连接')
        return
      }

      // 将token作为查询参数添加到URL
      const wsUrl = `${this.url}?token=${encodeURIComponent(token)}`
      this.socket = new WebSocket(wsUrl)

      // 设置事件处理器
      this.socket.onopen = this.handleOpen.bind(this)
      this.socket.onmessage = this.handleMessage.bind(this)
      this.socket.onclose = this.handleClose.bind(this)
      this.socket.onerror = this.handleError.bind(this)
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.scheduleReconnect()
    }
  }

  /**
   * 处理WebSocket连接打开事件
   */
  private handleOpen(): void {
    console.log('WebSocket连接已建立')
    this.connected.value = true
    this.reconnectAttempts = 0
    
    // 启动心跳
    this.startHeartbeat()
  }

  /**
   * 处理WebSocket消息接收事件
   */
  private handleMessage(event: MessageEvent): void {
    try {
      const message = JSON.parse(event.data) as WebSocketMessage<any>
      this.lastMessage.value = message

      // 调用对应类型的消息处理器
      const handler = this.messageHandlers.get(message.type)
      if (handler && message.data) {
        handler(message.data)
      }

      // 特殊处理心跳响应
      if (message.type === MESSAGE_TYPE.HEARTBEAT) {
        console.debug('收到心跳响应:', message.data)
      }
    } catch (error) {
      console.error('处理WebSocket消息失败:', error)
    }
  }

  /**
   * 处理WebSocket连接关闭事件
   */
  private handleClose(event: CloseEvent): void {
    console.log(`WebSocket连接已关闭: ${event.code} ${event.reason}`)
    this.connected.value = false
    this.stopHeartbeat()

    // 尝试重新连接
    if (!event.wasClean) {
      this.scheduleReconnect()
    }
  }

  /**
   * 处理WebSocket错误事件
   */
  private handleError(event: Event): void {
    console.error('WebSocket错误:', event)
    showFailMessage('WebSocket连接错误', '连接出现问题，正在尝试重新连接')
  }

  /**
   * 安排重新连接
   */
  private scheduleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重新连接 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      
      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval)
    } else {
      console.error('达到最大重连次数，放弃重连')
      showFailMessage('连接失败', '无法连接到聊天服务器，请刷新页面重试')
    }
  }

  /**
   * 启动心跳机制
   */
  private startHeartbeat(): void {
    this.stopHeartbeat()
    
    // 每30秒发送一次心跳
    this.heartbeatInterval = window.setInterval(() => {
      this.sendHeartbeat()
    }, 30000)
  }

  /**
   * 停止心跳机制
   */
  private stopHeartbeat(): void {
    if (this.heartbeatInterval !== null) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  /**
   * 发送心跳消息
   */
  private sendHeartbeat(): void {
    const heartbeat: WebSocketMessage<null> = {
      type: MESSAGE_TYPE.HEARTBEAT,
      data: null
    }
    this.sendMessage(heartbeat)
  }

  /**
   * 发送消息
   * @param message 要发送的消息对象
   * @returns 是否发送成功
   */
  public sendMessage<T>(message: WebSocketMessage<T>): boolean {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      console.error('WebSocket未连接，无法发送消息')
      return false
    }

    try {
      this.socket.send(JSON.stringify(message))
      return true
    } catch (error) {
      console.error('发送WebSocket消息失败:', error)
      return false
    }
  }

  /**
   * 发送聊天消息
   * @param chatMessage 聊天消息数据
   * @returns 是否发送成功
   */
  public sendChatMessage(chatMessage: ChatMessageData): boolean {
    const message: WebSocketMessage<ChatMessageData> = {
      type: MESSAGE_TYPE.CHAT,
      data: chatMessage
    }
    return this.sendMessage(message)
  }

  /**
   * 注册消息处理器
   * @param messageType 消息类型
   * @param handler 处理函数
   */
  public registerMessageHandler(messageType: string, handler: (data: any) => void): void {
    this.messageHandlers.set(messageType, handler)
  }

  /**
   * 注销消息处理器
   * @param messageType 消息类型
   */
  public unregisterMessageHandler(messageType: string): void {
    this.messageHandlers.delete(messageType)
  }

  /**
   * 关闭WebSocket连接
   */
  public disconnect(): void {
    this.stopHeartbeat()
    
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    
    this.connected.value = false
  }
}

// 导出单例实例
export const websocketService = new WebSocketService()