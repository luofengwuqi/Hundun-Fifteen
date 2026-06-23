 import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getUserInfoAPI, getUserModelStatus, updateUserModelStatus } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  //token - 从 localStorage 初始化
  const token = ref(localStorage.getItem('token') || '')
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }
  //userInfo - 从 localStorage 初始化
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = ref(userInfoStr ? JSON.parse(userInfoStr) : {})
  const userModelStatus = ref({})
  const setUserInfo = (newUserInfo) => {
    userInfo.value = newUserInfo
    if (Object.keys(newUserInfo).length > 0) {
      localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
    } else {
      localStorage.removeItem('userInfo')
    }
  }
  const initUserInfo = async () => {
    try {
      const res = await getUserInfoAPI()
      if (res.data) {
        setUserInfo(res.data)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  const initUserModelStatus = async () => {
    try {
      const res = await getUserModelStatus()
      if (res.data) {
        userModelStatus.value = res.data
      }
    } catch (error) {
      console.error('获取用户模型状态失败:', error)
    }
  }

  // 启用分类模块
  const enableCategory = async (categoryKey) => {
    try {
      // 获取当前用户ID
      const userId = userInfo.value.id
      if (!userId) {
        throw new Error('用户ID不存在')
      }

      // 构建完整的状态更新数据（合并现有状态，避免覆盖其他字段）
      const updateData = {
        userId,
        ...userModelStatus.value,  // 保留现有的所有状态
        [categoryKey]: 1           // 更新目标字段为启用
      }

      console.log(`启用分类 ${categoryKey}, 用户ID: ${userId}`)
      console.log('发送的完整数据:', updateData)
      await updateUserModelStatus(updateData)

      // 更新本地状态
      if (userModelStatus.value) {
        userModelStatus.value[categoryKey] = 1
      }

      console.log(`分类 ${categoryKey} 已启用`)
    } catch (error) {
      console.error('启用分类失败:', error)
      throw error
    }
  }

  // 禁用分类模块
  const disableCategory = async (categoryKey) => {
    try {
      const userId = userInfo.value.id
      if (!userId) {
        throw new Error('用户ID不存在')
      }

      const updateData = {
        userId,
        ...userModelStatus.value,
        [categoryKey]: 0
      }

      await updateUserModelStatus(updateData)

      if (userModelStatus.value) {
        userModelStatus.value[categoryKey] = 0
      }
    } catch (error) {
      console.error('禁用分类失败:', error)
      throw error
    }
  }


  const clearUserModelStatus = () => {
    userModelStatus.value = {}
  }

  return {
    token,
    setToken,
    userInfo,
    setUserInfo,
    initUserInfo,
    userModelStatus,
    initUserModelStatus,
    clearUserModelStatus,
    enableCategory,
    disableCategory
  }
})
