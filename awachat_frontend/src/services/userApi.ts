import type {
  AcceptFriendRequestRequest,
  DeleteFriendRequest,
  LoginRequest,
  LoginResponse,
  LogoutResponse,
  RegisterRequest,
  UserData,
} from '@/dto/userDto'
import { api } from './api'
import type { AxiosResponse } from 'axios'
import type { ApiResponse } from '@/dto/base'

export const registerApi = (r: RegisterRequest) =>
  api.post<RegisterRequest, AxiosResponse<ApiResponse<null>>>('/user/register', r)

export const loginApi = (r: LoginRequest) =>
  api.post<LoginRequest, AxiosResponse<ApiResponse<LoginResponse>>>('/user/login', r)

export const logoutApi = () =>
  api.post<null, AxiosResponse<ApiResponse<LogoutResponse>>>('/user/logout')

// 修改getUserInfoApi函数，添加username参数
export const getUserInfoApi = (userId?: number, username?: string) =>
  api.get<ApiResponse<UserData>>(
    `/user/info${userId ? `?userId=${userId}` : ''}${username ? `?username=${username}` : ''}`,
  )

export const acceptFriendRequestApi = (r: AcceptFriendRequestRequest) =>
  api.post<AcceptFriendRequestRequest, AxiosResponse<ApiResponse<null>>>(
    '/user/acceptFriendRequest',
    r,
  )

export const deleteFriendApi = (r: DeleteFriendRequest) =>
  api.post<DeleteFriendRequest, AxiosResponse<ApiResponse<null>>>('/user/deleteFriend', r)
