/**
 * @file request.js
 * @description 基于 Axios 的 HTTP 请求封装
 * @author SmartHealth Team
 * @date 2024
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

/**
 * 创建 Axios 实例
 * @type {import('axios').AxiosInstance}
 */
const request = axios.create({
  baseURL: '/api',    // API 基础路径
  timeout: 10000      // 请求超时时间（毫秒）
})

/**
 * 请求拦截器
 * 在发送请求之前做一些处理，如添加请求头
 */
request.interceptors.request.use(
  (config) => {
    // 获取用户状态管理
    const userStore = useUserStore()
    
    // 如果存在 token，添加 Authorization 请求头
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // 如果存在 DashScope API Key，添加自定义请求头
    const apiKey = localStorage.getItem('dashscope_api_key')
    if (apiKey) {
      config.headers['X-DashScope-API-Key'] = apiKey
    }
    
    return config
  },
  (error) => {
    // 请求错误处理
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 在接收到响应后做一些处理，如统一错误处理
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 兼容有 code 字段和无 code 字段两种返回格式
    // code 为 undefined（无 code 字段）或 code 为 200 表示成功
    if (res.code === undefined || res.code === 200) {
      return res
    } else {
      // 业务错误，显示错误提示
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    // 401 未授权，跳转到登录页
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    
    // 构建错误信息，优先使用后端返回的 error 或 message 字段
    const msg = error.response?.data?.error 
              || error.response?.data?.message 
              || error.message 
              || '网络错误'
    
    // 显示错误提示
    ElMessage.error(msg)
    
    return Promise.reject(error)
  }
)

/**
 * 导出封装后的 Axios 实例
 * @module request
 */
export default request
