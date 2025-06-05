import type { LoginRequest, LoginResponse, LogoutResponse, RegisterRequest } from '@/dto/userDto'
import { api } from './api'
import type { AxiosResponse } from 'axios'
import type { ApiResponse } from '@/dto/base'

export const registerApi = (r: RegisterRequest) =>
  api.post<RegisterRequest, AxiosResponse<ApiResponse<null>>>('/user/register', r)

export const loginApi = (r: LoginRequest) =>
  api.post<LoginRequest, AxiosResponse<ApiResponse<LoginResponse>>>('/user/login', r)

export const logout = () => api.post<AxiosResponse<ApiResponse<LogoutResponse>>>('/user/logout')
