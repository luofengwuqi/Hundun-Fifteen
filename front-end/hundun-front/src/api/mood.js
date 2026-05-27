import request from '@/utils/request.js'

export const insertMoodAPI = async (dto) => {
  return await request.post('/mood/health', dto)
}

export const updateMoodAPI = async (dto) => {
  return await request.put('/mood/health', dto)
}
