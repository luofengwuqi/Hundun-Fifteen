import request from '@/utils/request.js'

/**
 * 创建重要备忘
 * @param {Object} dto - 重要备忘数据对象
 */
export const createMemo = async (dto) => {
  return await request.post('/memo/important', dto)
}

/**
 * 删除重要备忘
 * @param {number} id - 备忘ID
 */
export const deleteMemo = async (id) => {
  return await request.delete(`/memo/important/${id}`)
}

/**
 * 更新重要备忘
 * @param {number} id - 备忘ID
 * @param {Object} dto - 更新的重要备忘数据对象
 */
export const updateMemo = async (id, dto) => {
  return await request.put(`/memo/important/${id}`, dto)
}

/**
 * 查询单个重要备忘
 * @param {number} id - 备忘ID
 */
export const getMemo = async (id) => {
  return await request.get(`/memo/important/${id}`)
}

/**
 * 查询当前用户的所有重要备忘
 */
export const getUserMemos = async () => {
  return await request.get('/memo/important')
}