import type { LoginRequest, RegisterRequest, UserData } from '@/dto/userDto'
import { api } from './api'
import type { AxiosResponse } from 'axios'
import type { ApiResponse } from '@/dto/base'

export const registerApi = (r: RegisterRequest) =>
  api.post<RegisterRequest, AxiosResponse<ApiResponse<null>>>('/user/register', r)

export const loginApi = (r: LoginRequest) =>
  api.post<LoginRequest, AxiosResponse<ApiResponse<UserData>>>('/user/login', r)
