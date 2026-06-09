import request from '@/utils/request'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

export const userLogin = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const userRegister = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const getUserInfo = () => {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

export const changePassword = (data) => {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data
  })
}

export const getAllUsers = () => {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

export const resetUserPassword = (userId, data) => {
  return request({
    url: `/admin/users/${userId}/reset-password`,
    method: 'post',
    data
  })
}

export const getCheckinStatus = () => {
  return request({
    url: '/checkin/status',
    method: 'get'
  })
}

export const dailyCheckin = () => {
  return request({
    url: '/checkin/daily',
    method: 'post'
  })
}

export const getHealthRecord = () => {
  return request({
    url: '/health-record',
    method: 'get'
  })
}

export const createHealthRecord = (data) => {
  return request({
    url: '/health-record',
    method: 'post',
    data
  })
}

export const updateHealthRecord = (data) => {
  return request({
    url: '/health-record',
    method: 'put',
    data
  })
}

export const checkHealthRecordExists = () => {
  return request({
    url: '/health-record/exists',
    method: 'get'
  })
}

export const getCheckinHistory = () => {
  return request({
    url: '/checkin/history',
    method: 'get'
  })
}

export const getWeightHistory = (params) => {
  return request({
    url: '/weight-record/history',
    method: 'get',
    params
  })
}

export const recordWeight = (data) => {
  return request({
    url: '/weight-record',
    method: 'post',
    data
  })
}

export const deleteWeightRecord = (id) => {
  return request({
    url: `/weight-record/${id}`,
    method: 'delete'
  })
}

export const updateWeightRecord = (id, data) => {
  return request({
    url: `/weight-record/${id}`,
    method: 'put',
    data
  })
}

export const getWeightTrend = () => {
  return request({
    url: '/weight-record/trend',
    method: 'get'
  })
}

export const generateAiPlan = () => {
  return request({
    url: '/ai-plan/generate',
    method: 'post',
    timeout: 120000
  })
}

export const getAiPlanHistory = () => {
  return request({
    url: '/ai-plan/history',
    method: 'get'
  })
}

export const getLatestAiPlan = () => {
  return request({
    url: '/ai-plan/latest',
    method: 'get'
  })
}

export const deleteAiPlan = (id) => {
  return request({
    url: `/ai-plan/${id}`,
    method: 'delete'
  })
}

export const saveClockRecord = (data) => {
  return request({
    url: '/clock-record/save',
    method: 'post',
    data
  })
}

export const getTodayClockRecord = (planId) => {
  return request({
    url: '/clock-record/today',
    method: 'get',
    params: { planId }
  })
}

export const getWeeklyStats = () => {
  return request({
    url: '/clock-record/week',
    method: 'get'
  })
}

export const getClockRecordsByRange = (startDate, endDate) => {
  return request({
    url: '/clock-record/range',
    method: 'get',
    params: { startDate, endDate }
  })
}

export const deleteClockRecord = (id) => {
  return request({
    url: `/clock-record/${id}`,
    method: 'delete'
  })
}

export const exportAiPlanPdf = async (planId = null) => {
  const userStore = useUserStore()
  const url = planId ? `/api/ai-plan/export/${planId}` : '/api/ai-plan/export/latest'
  
  try {
    const response = await axios({
      url: url,
      method: 'get',
      headers: {
        Authorization: `Bearer ${userStore.token}`
      },
      responseType: 'blob',
      timeout: 60000
    })
    
    const contentDisposition = response.headers['content-disposition']
    let filename = '健康计划.pdf'
    if (contentDisposition) {
      const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (match && match[1]) {
        filename = decodeURIComponent(match[1].replace(/['"]/g, ''))
      }
    }
    
    const urlObject = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = urlObject
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(urlObject)
    
    return { success: true, message: '导出成功' }
  } catch (error) {
    console.error('导出PDF失败', error)
    throw new Error(error.response?.data?.message || error.message || '导出失败')
  }
}
