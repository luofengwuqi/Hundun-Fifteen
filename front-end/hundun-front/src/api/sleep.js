import request from '@/utils/request.js'

export const insertSleepAPI = async (dto) => {
  return await request.post('/sleep/health', dto)
}

export const updateSleepAPI = async (dto) => {
  return await request.put('/sleep/health', dto)
}
