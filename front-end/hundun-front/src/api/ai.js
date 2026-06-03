import request from '@/utils/request.js'

export const analyzeDiaryAPI = async (diaryText) => {
  return await request.post('/ai/diary/analyze', { diary: diaryText }, { timeout: 60000 })
}

export const confirmDiaryAPI = async (data) => {
  return await request.post('/ai/diary/confirm', data, { timeout: 30000 })
}
