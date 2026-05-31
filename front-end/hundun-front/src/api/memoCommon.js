import request from '@/utils/request.js'

/**
 * 创建普通备忘
 * @param {Object} dto - 备忘数据对象
 */
export const createMemo = async (dto) => {
  return await request.post('/memo/common', dto)
}

/**
 * 删除普通备忘
 * @param {number} id - 备忘ID
 */
export const deleteMemo = async (id) => {
  return await request.delete(`/memo/common/${id}`)
}

/**
 * 更新普通备忘
 * @param {number} id - 备忘ID
 * @param {Object} dto - 更新的备忘数据对象
 */
export const updateMemo = async (id, dto) => {
  return await request.put(`/memo/common/${id}`, dto)
}

/**
 * 查询单个普通备忘
 * @param {number} id - 备忘ID
 */
export const getMemo = async (id) => {
  return await request.get(`/memo/common/${id}`)
}

/**
 * 查询当前用户的所有普通备忘
 */
/**
 * 查询当前用户的普通备忘
 * @param {Object} [params]
 * @param {string} [params.completion] pending-未完成，done-已完成；不传为全部
 */
export const getUserMemos = async (params) => {
  return await request.get('/memo/common', { params })
}

/**
 * 手动执行定时任务：发送邮件通知给开启邮件通知功能的用户
 */
export const sendMemoEmailAPI = async () => {
  return await request.post('/memo/common/email')
}