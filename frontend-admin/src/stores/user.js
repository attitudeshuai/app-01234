import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value.role === 'ADMIN')
  const username = computed(() => userInfo.value.username || '')
  const realName = computed(() => userInfo.value.realName || '')
  const departmentId = computed(() => userInfo.value.departmentId)
  const departmentName = computed(() => userInfo.value.departmentName || '')

  async function login(loginData) {
    const res = await loginApi(loginData)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      const info = { ...userInfo.value, ...res.data }
      userInfo.value = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    } catch (e) {
      // ignore
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  return {
    token, userInfo, isLoggedIn, isAdmin, username, realName,
    departmentId, departmentName,
    login, fetchUserInfo, logout
  }
})
