/**
 * 用户信息
 */
export interface UserData {
  userId: number
  username: string
  nickname: string
  description: string
  avatar: string | null
  createdAt: string
  lastOnlineAt: string
  role: string
  banUntil: string
  extendedData: object
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  nickname: string
  password: string
}
