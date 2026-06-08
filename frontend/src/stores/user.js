import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const userInfo = ref(null)

    const setToken = (newToken) => {
      token.value = newToken
    }

    const setUserInfo = (info) => {
      userInfo.value = info
    }

    const logout = () => {
      token.value = ''
      userInfo.value = null
    }

    const isAdmin = () => {
      return userInfo.value?.role === 1
    }

    return {
      token,
      userInfo,
      setToken,
      setUserInfo,
      logout,
      isAdmin
    }
  },
  {
    persist: true
  }
)
