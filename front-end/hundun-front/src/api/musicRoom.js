import request from '@/utils/request.js'

export const initMusicRoomDataAPI = async () => {
  return await request.get('/musicRoom')
}

export const createMusicRoomAPI = async (dto) => {
  return await request.post('/musicRoom', dto)
}

export const joinMusicRoomAPI = async (dto) => {
  return await request.post('/musicRoom/join', dto)
}

export const exitMusicRoomAPI = async () => {
  return await request.delete('/musicRoom')
}

export const addSongToRoomAPI = async (dto) => {
  return await request.post('/musicRoom/song', dto)
}

export const deleteSongFromRoomAPI = async (dto) => {
  return await request.delete('/musicRoom/song', { data: dto })
}
