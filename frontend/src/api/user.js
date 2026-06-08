import request from '@/utils/request'

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
