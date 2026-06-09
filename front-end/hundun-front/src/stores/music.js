import { ref } from 'vue'
import { defineStore } from 'pinia'
import {
  deleteMusicAPI,
  getAllMusicAPI,
  getMusicByIdAPI,
  insertMusicAPI,
  updateMusicAPI,
  uploadAudioAPI,
  uploadCoverAPI,
} from '@/api/music.js'

export const useMusicStore = defineStore('music', () => {
  const musicList = ref([])
  const currentMusic = ref(null)
  const loading = ref(false)

  const currentIndex = ref(-1)
  const playMode = ref('sequential')
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(0.7)

  let audioElement = null
  const endedCallbacks = []

  const onEnded = (cb) => {
    endedCallbacks.push(cb)
  }

  const initAudio = () => {
    if (audioElement) return
    audioElement = new Audio()
    audioElement.volume = volume.value
    audioElement.addEventListener('timeupdate', () => {
      currentTime.value = audioElement.currentTime
    })
    audioElement.addEventListener('loadedmetadata', () => {
      duration.value = audioElement.duration
    })
    audioElement.addEventListener('ended', () => {
      isPlaying.value = false
      endedCallbacks.forEach(cb => cb())
      handleSongEnd()
    })
    audioElement.addEventListener('play', () => {
      isPlaying.value = true
    })
    audioElement.addEventListener('pause', () => {
      isPlaying.value = false
    })
  }

  const playSong = (index) => {
    if (!musicList.value.length) return
    const idx = (index + musicList.value.length) % musicList.value.length
    currentIndex.value = idx
    currentMusic.value = musicList.value[idx]
    initAudio()
    audioElement.src = currentMusic.value.audioUrl
    audioElement.play().catch(() => {
      const resume = () => {
        audioElement.play().catch(() => {})
        document.removeEventListener('click', resume)
        document.removeEventListener('touchstart', resume)
      }
      document.addEventListener('click', resume, { once: true })
      document.addEventListener('touchstart', resume, { once: true })
    })
  }

  const togglePlay = () => {
    if (!audioElement || currentIndex.value < 0) {
      if (musicList.value.length) playSong(0)
      return
    }
    if (audioElement.paused) {
      audioElement.play().catch(() => {})
    } else {
      audioElement.pause()
    }
  }

  const prevSong = () => {
    if (!musicList.value.length) return
    let idx
    if (playMode.value === 'random') {
      idx = Math.floor(Math.random() * musicList.value.length)
    } else {
      idx = currentIndex.value - 1
      if (idx < 0) idx = musicList.value.length - 1
    }
    playSong(idx)
  }

  const nextSong = () => {
    if (!musicList.value.length) return
    let idx
    if (playMode.value === 'random') {
      idx = Math.floor(Math.random() * musicList.value.length)
    } else {
      idx = currentIndex.value + 1
      if (idx >= musicList.value.length) idx = 0
    }
    playSong(idx)
  }

  const handleSongEnd = () => {
    if (playMode.value === 'single') {
      if (audioElement) {
        audioElement.currentTime = 0
        audioElement.play().catch(() => {})
      }
    } else {
      nextSong()
    }
  }

  const seekTo = (time) => {
    if (audioElement) {
      audioElement.currentTime = time
    }
  }

  const togglePlayMode = () => {
    if (playMode.value === 'sequential') playMode.value = 'random'
    else if (playMode.value === 'random') playMode.value = 'single'
    else playMode.value = 'sequential'
  }

  const setVolume = (val) => {
    volume.value = val
    if (audioElement) audioElement.volume = val
  }

  const stop = () => {
    if (audioElement) {
      audioElement.pause()
    }
    isPlaying.value = false
    currentTime.value = 0
    currentIndex.value = -1
    currentMusic.value = null
  }

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
      musicList.value = musicList.value.filter((item) => item.id !== id)
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
    return await uploadCoverAPI(formData)
  }

  return {
    musicList,
    currentMusic,
    loading,
    currentIndex,
    playMode,
    isPlaying,
    currentTime,
    duration,
    volume,
    fetchAllMusic,
    fetchMusicById,
    addMusic,
    removeMusic,
    editMusic,
    uploadAudio,
    uploadCover,
    playSong,
    togglePlay,
    prevSong,
    nextSong,
    seekTo,
    togglePlayMode,
    setVolume,
    onEnded,
    stop,
  }
})
