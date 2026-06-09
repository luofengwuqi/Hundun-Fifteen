import { ref } from 'vue'
import { defineStore } from 'pinia'
import {
  initMusicRoomDataAPI,
  createMusicRoomAPI,
  joinMusicRoomAPI,
  exitMusicRoomAPI,
  addSongToRoomAPI,
  deleteSongFromRoomAPI,
} from '@/api/musicRoom.js'

export const useMusicRoomStore = defineStore('musicRoom', () => {
  const room = ref(null)
  const loading = ref(false)

  const fetchRoomData = async () => {
    loading.value = true
    try {
      const res = await initMusicRoomDataAPI()
      if (res.code === 200 || res.code === 0) {
        room.value = res.data || null
      }
      return res
    } finally {
      loading.value = false
    }
  }

  const createRoom = async (roomSecret) => {
    const res = await createMusicRoomAPI({ roomSecret })
    if (res.code === 200 || res.code === 0) {
      await fetchRoomData()
    }
    return res
  }

  const joinRoom = async (roomSecret) => {
    const res = await joinMusicRoomAPI({ roomSecret })
    if (res.code === 200 || res.code === 0) {
      await fetchRoomData()
    }
    return res
  }

  const exitRoom = async () => {
    const res = await exitMusicRoomAPI()
    if (res.code === 200 || res.code === 0) {
      room.value = null
    }
    return res
  }

  const addSong = async (songId) => {
    const res = await addSongToRoomAPI({ songId })
    if (res.code === 200 || res.code === 0) {
      await fetchRoomData()
    }
    return res
  }

  const deleteSong = async (songId) => {
    const res = await deleteSongFromRoomAPI({ songId })
    if (res.code === 200 || res.code === 0) {
      await fetchRoomData()
    }
    return res
  }

  return {
    room,
    loading,
    fetchRoomData,
    createRoom,
    joinRoom,
    exitRoom,
    addSong,
    deleteSong,
  }
})
