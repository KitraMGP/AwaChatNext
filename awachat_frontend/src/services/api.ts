import type { ApiResponse } from '@/dto/base'
import axios, { AxiosError, type AxiosResponse } from 'axios'
import { ElNotification } from 'element-plus'

export const api = axios.create({
  baseURL: '/api/',
  timeout: 10000,
})

/**
 * 根据ApiResponse获取错误信息
 */
export const getErrorMsg = (response: AxiosResponse<ApiResponse<unknown>>) => {
  return response.data.msg
}

/**
 * 检查请求是否成功。若成功返回true，否则返回false
 */
export const checkSuccessful = (response: AxiosResponse<ApiResponse<unknown>>) => {
  return response.data.code === 200
}
/**
 * 传入标题和错误，横幅展示错误信息
 */
export const showFailMessage = (title: string, e: AxiosError | string | unknown) => {
  let message = ''
  // 对错误的类型进行判断
  if (axios.isAxiosError(e)) {
    message = e.message
  } else if (typeof e === 'string') {
    message = e
  } else if (e instanceof Error) {
    message = e.message
  } else {
    message = '未知错误'
    console.error('未知错误：', e)
  }
  console.error(message)
  ElNotification({
    type: 'error',
    title: title,
    message: message,
  })
}

/**
 * 横幅展示成功信息
 */
export const showSuccessfulMessage = (title: string) => {
  ElNotification({
    type: 'success',
    title: title,
    message: '',
    duration: 2000,
  })
}
