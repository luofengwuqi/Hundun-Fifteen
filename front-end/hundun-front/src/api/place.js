import request from '@/utils/request.js'

export const insertPlaceHealthAPI = async (dto) => {
  return await request.post('/place/health', dto)
}

export const updatePlaceHealthAPI = async (dto) => {
  return await request.put('/place/health', dto)
}

export const insertPlaceFinanceAPI = async (dto) => {
  return await request.post('/place/finance', dto)
}

export const updatePlaceFinanceAPI = async (dto) => {
  return await request.put('/place/finance', dto)
}
