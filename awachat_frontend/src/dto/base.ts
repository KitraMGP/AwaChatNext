// 定义会话类型接口
export interface Conversation {
  id: number
  name: string
  lastMessage: string
  time: string
  unread: number
}
