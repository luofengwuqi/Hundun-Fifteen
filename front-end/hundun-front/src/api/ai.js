import request from '@/utils/request.js'

export const analyzeDiaryAPI = async (diaryText) => {
  return await request.post('/ai/diary/analyze', { diary: diaryText }, { timeout: 60000 })
}
