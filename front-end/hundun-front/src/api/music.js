import request from '@/utils/request.js'

export const insertMusicAPI = async (dto) => {
  return await request.post('/music', dto)
}

export const deleteMusicAPI = async (id) => {
  return await request.delete(`/music/${id}`)
}

export const updateMusicAPI = async (id, dto) => {
  return await request.put(`/music/${id}`, dto)
}

export const getMusicByIdAPI = async (id) => {
  return await request.get(`/music/${id}`)
}

export const getAllMusicAPI = async () => {
  return await request.get('/music')
}

export const uploadAudioAPI = async (formData) => {
  return await request.post('/music/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const uploadCoverAPI = async (formData) => {
  return await request.post('/music/upload/cover', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 从OSS同步音乐数据到数据库
export const syncMusicFromOssAPI = async () => {
  return await request.post('/music/task/oss')
}
