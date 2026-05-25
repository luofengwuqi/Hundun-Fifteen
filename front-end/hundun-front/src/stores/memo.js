import { ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createMemo as createCommonMemo,
  deleteMemo as deleteCommonMemo,
  updateMemo as updateCommonMemo,
  getMemo as getCommonMemo,
  getUserMemos as getCommonUserMemos
} from '@/api/memoCommon'
import {
  createMemo as createImportantMemo,
  deleteMemo as deleteImportantMemo,
  updateMemo as updateImportantMemo,
  getMemo as getImportantMemo,
  getUserMemos as getImportantUserMemos
} from '@/api/memoImportant'

export const useMemoStore = defineStore('memo', () => {
  // 普通备忘列表
  const commonMemos = ref([])
  
  // 重要备忘列表
  const importantMemos = ref([])

  // ==================== 普通备忘操作 ====================
  
  /**
   * 获取当前用户的所有普通备忘
   */
  const getCommonMemos = async (completion) => {
    try {
      const params =
        completion && completion !== 'all' ? { completion } : undefined
      const res = await getCommonUserMemos(params)
      commonMemos.value = res.data || []
      return res
    } catch (error) {
      console.error('获取普通备忘列表失败:', error)
      throw error
    }
  }

  /**
   * 创建普通备忘
   * @param {Object} dto - 备忘数据
   */
  const addCommonMemo = async (dto) => {
    try {
      const res = await createCommonMemo(dto)
      // 创建成功后重新获取列表
      await getCommonMemos()
      return res
    } catch (error) {
      console.error('创建普通备忘失败:', error)
      throw error
    }
  }

  /**
   * 更新普通备忘
   * @param {number} id - 备忘ID
   * @param {Object} dto - 更新的备忘数据
   */
  const editCommonMemo = async (id, dto, completion) => {
    try {
      const res = await updateCommonMemo(id, dto)
      await getCommonMemos(completion ?? 'all')
      return res
    } catch (error) {
      console.error('更新普通备忘失败:', error)
      throw error
    }
  }

  /**
   * 删除普通备忘
   * @param {number} id - 备忘ID
   */
  const removeCommonMemo = async (id) => {
    try {
      const res = await deleteCommonMemo(id)
      // 删除成功后从列表中移除
      const index = commonMemos.value.findIndex(memo => memo.id === id)
      if (index > -1) {
        commonMemos.value.splice(index, 1)
      }
      return res
    } catch (error) {
      console.error('删除普通备忘失败:', error)
      throw error
    }
  }

  /**
   * 获取单个普通备忘
   * @param {number} id - 备忘ID
   */
  const getSingleCommonMemo = async (id) => {
    try {
      const res = await getCommonMemo(id)
      return res
    } catch (error) {
      console.error('获取普通备忘详情失败:', error)
      throw error
    }
  }

  // ==================== 重要备忘操作 ====================

  /**
   * 获取当前用户的所有重要备忘
   */
  const getImportantMemos = async () => {
    try {
      const res = await getImportantUserMemos()
      importantMemos.value = res.data || []
      return res
    } catch (error) {
      console.error('获取重要备忘列表失败:', error)
      throw error
    }
  }

  /**
   * 创建重要备忘
   * @param {Object} dto - 备忘数据
   */
  const addImportantMemo = async (dto) => {
    try {
      const res = await createImportantMemo(dto)
      // 创建成功后重新获取列表
      await getImportantMemos()
      return res
    } catch (error) {
      console.error('创建重要备忘失败:', error)
      throw error
    }
  }

  /**
   * 更新重要备忘
   * @param {number} id - 备忘ID
   * @param {Object} dto - 更新的备忘数据
   */
  const editImportantMemo = async (id, dto) => {
    try {
      const res = await updateImportantMemo(id, dto)
      // 更新成功后重新获取列表
      await getImportantMemos()
      return res
    } catch (error) {
      console.error('更新重要备忘失败:', error)
      throw error
    }
  }

  /**
   * 删除重要备忘
   * @param {number} id - 备忘ID
   */
  const removeImportantMemo = async (id) => {
    try {
      const res = await deleteImportantMemo(id)
      // 删除成功后从列表中移除
      const index = importantMemos.value.findIndex(memo => memo.id === id)
      if (index > -1) {
        importantMemos.value.splice(index, 1)
      }
      return res
    } catch (error) {
      console.error('删除重要备忘失败:', error)
      throw error
    }
  }

  /**
   * 获取单个重要备忘
   * @param {number} id - 备忘ID
   */
  const getSingleImportantMemo = async (id) => {
    try {
      const res = await getImportantMemo(id)
      return res
    } catch (error) {
      console.error('获取重要备忘详情失败:', error)
      throw error
    }
  }

  return {
    // 状态
    commonMemos,
    importantMemos,
    
    // 普通备忘方法
    getCommonMemos,
    addCommonMemo,
    editCommonMemo,
    removeCommonMemo,
    getSingleCommonMemo,
    
    // 重要备忘方法
    getImportantMemos,
    addImportantMemo,
    editImportantMemo,
    removeImportantMemo,
    getSingleImportantMemo
  }
})
