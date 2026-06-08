import { ref } from 'vue'
import { defineStore } from 'pinia'
import {
  insertMusicAPI,
  deleteMusicAPI,
  updateMusicAPI,
  getMusicByIdAPI,
  getAllMusicAPI,
  uploadAudioAPI,
  uploadCoverAPI
} from '@/api/music.js'

export const useMusicStore = defineStore('music', () => {
  const musicList = ref([])
  const currentMusic = ref(null)
  const loading = ref(false)

  const fetchAllMusic = async () => {
    loading.value = true
    try {
      const res = await getAllMusicAPI()
      if (res.code === 200 || res.code === 0) {
        musicList.value = res.data || []
      }
      return res
    } finally {
      loading.value = false
    }
  }

  const fetchMusicById = async (id) => {
    loading.value = true
    try {
      const res = await getMusicByIdAPI(id)
      if (res.code === 200 || res.code === 0) {
        currentMusic.value = res.data || null
      }
      return res
    } finally {
      loading.value = false
    }
  }

  const addMusic = async (dto) => {
    const res = await insertMusicAPI(dto)
    if (res.code === 200 || res.code === 0) {
      await fetchAllMusic()
    }
    return res
  }

  const removeMusic = async (id) => {
    const res = await deleteMusicAPI(id)
    if (res.code === 200 || res.code === 0) {
      musicList.value = musicList.value.filter(item => item.id !== id)
    }
    return res
  }

  const editMusic = async (id, dto) => {
    const res = await updateMusicAPI(id, dto)
    if (res.code === 200 || res.code === 0) {
      await fetchAllMusic()
    }
    return res
  }

  const uploadAudio = async (file) => {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadAudioAPI(formData)
    return res
  }

  const uploadCover = async (file) => {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadCoverAPI(formData)
    return res
  }

  return {
    musicList, currentMusic, loading,
    fetchAllMusic, fetchMusicById,
    addMusic, removeMusic, editMusic, uploadAudio, uploadCover
  }
})
