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
  role: number
  banUntil: string
  isFriend?: boolean // 添加isFriend字段，表示是否为好友
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

export interface LoginResponse {
  userData: UserData
  satoken: string
}

export interface LogoutResponse {
  satoken: string
}

export interface AcceptFriendRequestRequest {
  originUserId: number
}
