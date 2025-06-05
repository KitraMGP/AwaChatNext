import type {
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

export const logoutApi = () => api.post<AxiosResponse<ApiResponse<LogoutResponse>>>('/user/logout')

export const getUserInfoApi = (userId?: number) =>
  api.get<ApiResponse<UserData>>(`/user/info${userId ? `?userId=${userId}` : ''}`)
