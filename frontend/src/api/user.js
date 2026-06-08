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
