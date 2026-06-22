<script setup>
import { useMusicStore } from '@/stores/music.js'
import { useMusicRoomStore } from '@/stores/musicRoom.js'
import { useUserStore } from '@/stores/user.js'
import { storeToRefs } from 'pinia'
import { onMounted, onUnmounted, ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import defaultCover from '@/assets/img/music/defaultCover.png'
import HundunHeader from '@/views/user/component/common/HundunHeader.vue'
import { loginRouterPush } from '@/utils/login/loginRouterPush.js'
import { ElMessage } from 'element-plus'

const musicStore = useMusicStore()
const musicRoomStore = useMusicRoomStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const {
  musicList, currentMusic, currentIndex,
  playMode, isPlaying, currentTime, duration
} = storeToRefs(musicStore)
const {
  fetchAllMusic, playSong, togglePlay,
  prevSong, nextSong, togglePlayMode, seekTo
} = musicStore

const { room, loading: roomLoading } = storeToRefs(musicRoomStore)
const { fetchRoomData, createRoom, joinRoom, exitRoom } = musicRoomStore

const drawerOpen = ref(false)
const joinFormVisible = ref(false)
const joinPassword = ref('')
const creating = ref(false)

const uploadExpanded = ref(false)
const uploadForm = ref({ title: '', uploaderName: '' })
const selectedFile = ref(null)
const selectedCoverFile = ref(null)
const coverPreviewUrl = ref('')
const uploading = ref(false)
const uploadRef = ref(null)
const coverUploadRef = ref(null)

const ownerId = route.params.ownerId

const searchQuery = ref('')
const sortKey = ref('default')
const isDragging = ref(false)
const sortOpen = ref(false)

const sortOptions = [
  { value: 'default', label: '默认排序' },
  { value: 'title', label: '按名称' },
  { value: 'uploader', label: '按上传者' },
  { value: 'duration', label: '按时长' },
]

const coverSrc = (item) => item?.coverUrl || defaultCover

const formatTime = (t) => {
  if (!t || isNaN(t)) return '0:00'
  const m = Math.floor(t / 60)
  const s = Math.floor(t % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

const progressPercent = computed(() => {
  if (!duration.value) return 0
  return (currentTime.value / duration.value) * 100
})

const filteredMusicList = computed(() => {
  let list = musicList.value
  if (searchQuery.value.trim()) {
    const kw = searchQuery.value.trim().toLowerCase()
    list = list.filter(
      (m) => m.title.toLowerCase().includes(kw) || m.uploaderName?.toLowerCase().includes(kw)
    )
  }
  if (sortKey.value === 'title') {
    list = [...list].sort((a, b) => a.title.localeCompare(b.title))
  } else if (sortKey.value === 'uploader') {
    list = [...list].sort((a, b) => (a.uploaderName || '').localeCompare(b.uploaderName || ''))
  } else if (sortKey.value === 'duration') {
    list = [...list].sort((a, b) => (a.duration || 0) - (b.duration || 0))
  }
  return list
})

const sortLabel = computed(() => sortOptions.find((o) => o.value === sortKey.value)?.label || '排序')

const toggleSort = () => {
  sortOpen.value = !sortOpen.value
}

const selectSort = (val) => {
  sortKey.value = val
  sortOpen.value = false
}

const handleSortClickOutside = (e) => {
  if (!e.target.closest('.sort-wrapper')) {
    sortOpen.value = false
  }
}

const getAudioDuration = (file) => {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const audio = new Audio(url)
    audio.addEventListener('loadedmetadata', () => {
      const duration = Math.round(audio.duration)
      URL.revokeObjectURL(url)
      resolve(duration)
    })
    audio.addEventListener('error', () => {
      URL.revokeObjectURL(url)
      reject(new Error('无法解析音频文件'))
    })
  })
}

const compressImage = (file, maxWidth = 800, quality = 0.7) => {
  return new Promise((resolve) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      const canvas = document.createElement('canvas')
      let { width, height } = img
      if (width > maxWidth) {
        height = height * (maxWidth / width)
        width = maxWidth
      }
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, width, height)
      canvas.toBlob(
        (blob) => {
          const compressedFile = new File([blob], file.name.replace(/\.\w+$/, '.jpg'), { type: 'image/jpeg' })
          URL.revokeObjectURL(url)
          resolve(compressedFile)
        },
        'image/jpeg',
        quality
      )
    }
    img.src = url
  })
}

const ALLOWED_IMAGE_EXT = ['.jpg', '.jpeg', '.png', '.gif', '.webp']
const ALLOWED_AUDIO_EXT = ['.mp3', '.wav', '.flac', '.ogg', '.aac', '.m4a', '.wma']

const checkExt = (name, allowed) => {
  const ext = name.substring(name.lastIndexOf('.')).toLowerCase()
  return allowed.includes(ext)
}

const handleUploadFileChange = (file) => {
  if (!checkExt(file.name, ALLOWED_AUDIO_EXT)) {
    ElMessage.warning(`不支持的文件格式，仅支持 ${ALLOWED_AUDIO_EXT.join(' / ')}`)
    if (uploadRef.value) uploadRef.value.handleRemove(file)
    return
  }
  selectedFile.value = file.raw
  if (file.raw) {
    getAudioDuration(file.raw).then(duration => {
      // duration detected
    }).catch(() => {})
  }
}

const handleUploadCoverChange = (file) => {
  if (!checkExt(file.name, ALLOWED_IMAGE_EXT)) {
    ElMessage.warning(`不支持的图片格式，仅支持 ${ALLOWED_IMAGE_EXT.join(' / ')}`)
    if (coverUploadRef.value) coverUploadRef.value.handleRemove(file)
    return
  }
  selectedCoverFile.value = file.raw
  coverPreviewUrl.value = URL.createObjectURL(file.raw)
}

const handleUploadSubmit = async () => {
  if (!uploadForm.value.title) {
    ElMessage.warning('请输入歌曲名')
    return
  }
  if (!selectedFile.value) {
    ElMessage.warning('请选择音频文件')
    return
  }

  uploading.value = true
  try {
    let coverUrl = ''
    if (selectedCoverFile.value) {
      const compressed = await compressImage(selectedCoverFile.value)
      const coverRes = await musicStore.uploadCover(compressed)
      if (coverRes.code !== 200 && coverRes.code !== 0) {
        ElMessage.error('封面上传失败')
        return
      }
      coverUrl = coverRes.data
    }

    const uploadRes = await musicStore.uploadAudio(selectedFile.value)
    if (uploadRes.code !== 200 && uploadRes.code !== 0) {
      ElMessage.error('音频上传失败')
      return
    }
    const audioUrl = uploadRes.data

    await musicStore.addMusic({
      title: uploadForm.value.title,
      duration: 0,
      coverUrl,
      audioUrl,
      uploaderName: uploadForm.value.uploaderName || userStore.userInfo.username || ''
    })
    ElMessage.success('上传成功(´｡• ᵕ •｡`)')
    uploadExpanded.value = false
    uploadForm.value = { title: '', uploaderName: '' }
    selectedFile.value = null
    selectedCoverFile.value = null
    coverPreviewUrl.value = ''
    if (uploadRef.value) uploadRef.value.clearFiles()
    if (coverUploadRef.value) coverUploadRef.value.clearFiles()
  } catch (err) {
    console.error('上传音乐失败:', err)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const handleSeek = (e) => {
  if (!duration.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  seekTo(ratio * duration.value)
}

const handleProgressMouseDown = (e) => {
  if (!duration.value) return
  isDragging.value = true
  handleSeek(e)
  document.addEventListener('mousemove', handleProgressMouseMove)
  document.addEventListener('mouseup', handleProgressMouseUp)
}

const handleProgressMouseMove = (e) => {
  if (!isDragging.value || !duration.value) return
  const track = document.querySelector('.np-progress-track')
  if (!track) return
  const rect = track.getBoundingClientRect()
  const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  seekTo(ratio * duration.value)
}

const handleProgressMouseUp = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', handleProgressMouseMove)
  document.removeEventListener('mouseup', handleProgressMouseUp)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', handleProgressMouseMove)
  document.removeEventListener('mouseup', handleProgressMouseUp)
  document.removeEventListener('click', handleSortClickOutside)
})

const toggleDrawer = () => {
  drawerOpen.value = !drawerOpen.value
}

const handleCreateRoom = async () => {
  if (room.value) {
    drawerOpen.value = false
    router.push(`/music/room/${room.value.userId}`)
    return
  }
  creating.value = true
  try {
    const secret = Math.random().toString(36).slice(2, 10)
    const res = await createRoom(secret)
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('房间创建成功')
      drawerOpen.value = false
      router.push(`/music/room/${userStore.userInfo.id}`)
    }
  } catch {
    try {
      await fetchRoomData()
      if (room.value) {
        drawerOpen.value = false
        router.push(`/music/room/${room.value.userId}`)
        return
      }
    } catch {
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    creating.value = false
  }
}

const handleJoinRoom = async () => {
  if (!joinPassword.value.trim()) {
    ElMessage.warning('请输入房间密码')
    return
  }
  try {
    const res = await joinRoom(joinPassword.value.trim())
    if (res.code === 200 || res.code === 0) {
      ElMessage.success('已加入房间')
      joinPassword.value = ''
      joinFormVisible.value = false
      drawerOpen.value = false
      if (room.value) {
        router.push(`/music/room/${room.value.userId}`)
      }
    }
  } finally {
    // noop
  }
}

const handleExitRoom = async () => {
  await exitRoom()
  ElMessage.success('已退出房间')
  router.push('/music/main')
}

onMounted(async () => {
  if (!musicList.value.length) fetchAllMusic()
  if (ownerId) {
    try {
      await fetchRoomData()
    } catch {
      // not in room yet
    }
  }
  document.addEventListener('click', handleSortClickOutside)
})

watch(() => route.params.ownerId, async (id) => {
  if (id) {
    try {
      await fetchRoomData()
    } catch {
      // noop
    }
  }
})
</script>

<template>
  <div class="music-page">
    <HundunHeader></HundunHeader>
    <div class="music-space">
      <div class="header">
        <button class="back-btn" @click="loginRouterPush()">← 返回</button>
        <h1 class="title">🎵 音乐空间</h1>
        <span class="count">{{ searchQuery ? `${filteredMusicList.length} / ${musicList.length}` : musicList.length }} 首</span>
      </div>

      <div v-if="currentMusic" class="now-playing">
        <div class="np-cover">
          <img :src="coverSrc(currentMusic)" alt="" />
          <div v-if="!isPlaying" class="np-pause" @click="togglePlay">▶</div>
        </div>
        <div class="np-info">
          <div class="np-title">{{ currentMusic.title }}</div>
          <div class="np-uploader">{{ currentMusic.uploaderName }}</div>
          <div class="np-controls">
            <button class="np-btn" @click="prevSong">⏮</button>
            <button class="np-btn play" @click="togglePlay">
              {{ isPlaying ? '⏸' : '▶' }}
            </button>
            <button class="np-btn" @click="nextSong">⏭</button>
            <button class="np-btn mode" @click="togglePlayMode">
              {{ playMode === 'single' ? '↺' : playMode === 'random' ? '↯' : '↻' }}
            </button>
          </div>
          <div class="np-progress">
            <span class="np-time">{{ formatTime(currentTime) }}</span>
            <div
              class="np-progress-track"
              @mousedown.prevent="handleProgressMouseDown"
              @click="handleSeek"
            >
              <div class="np-progress-fill" :style="{ width: progressPercent + '%' }"></div>
              <div
                class="np-progress-thumb"
                :class="{ active: isDragging }"
                :style="{ left: progressPercent + '%' }"
              ></div>
            </div>
            <span class="np-time">{{ formatTime(duration) }}</span>
          </div>
        </div>
      </div>

      <div class="filter-bar">
        <input
          v-model="searchQuery"
          class="filter-input"
          placeholder="搜索歌曲或上传者..."
        />
        <div class="sort-wrapper">
          <div class="sort-trigger" :class="{ active: sortOpen, selected: sortKey !== 'default' }" @click.stop="toggleSort">
            <span>{{ sortLabel }}</span>
            <span class="sort-arrow" :class="{ up: sortOpen }">▼</span>
          </div>
          <Transition name="sort-drop">
            <div v-if="sortOpen" class="sort-dropdown">
              <div
                v-for="opt in sortOptions"
                :key="opt.value"
                class="sort-option"
                :class="{ checked: opt.value === sortKey }"
                @click="selectSort(opt.value)"
              >{{ opt.label }}</div>
            </div>
          </Transition>
        </div>
      </div>

      <div class="list">
        <div
          v-for="(item, idx) in filteredMusicList"
          :key="item.id"
          class="song-item"
          :class="{ active: item.id === currentMusic?.id }"
          @click="playSong(musicList.findIndex((m) => m.id === item.id))"
        >
          <img :src="coverSrc(item)" alt="" class="item-cover" />
          <div class="item-info">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-meta">{{ item.uploaderName }} · {{ formatTime(item.duration) }}</div>
          </div>
          <div v-if="item.id === currentMusic?.id && isPlaying" class="playing-indicator">♫</div>
        </div>
        <div v-if="!filteredMusicList.length" class="list-empty">没有找到匹配的歌曲</div>
      </div>
    </div>

    <!-- drawer toggle button -->
    <button class="drawer-toggle" :class="{ open: drawerOpen }" @click="toggleDrawer">
      <span class="toggle-icon">{{ drawerOpen ? '✕' : '☰' }}</span>
    </button>

    <!-- drawer overlay -->
    <Transition name="drawer-fade">
      <div v-if="drawerOpen" class="drawer-overlay" @click="drawerOpen = false"></div>
    </Transition>

    <!-- drawer panel -->
    <Transition name="drawer-slide">
      <div v-if="drawerOpen" class="drawer-panel">
        <div class="drawer-header">
          <h2 class="drawer-title">抽屉</h2>
        </div>

        <div class="drawer-body">
          <!-- music room section -->
          <div class="section">
            <div class="section-header">
              <span class="section-icon">🎶</span>
              <span class="section-label">音乐房间</span>
            </div>

            <!-- not in room -->
            <template v-if="!room">
              <button class="drawer-btn" @click="handleCreateRoom" :disabled="creating || roomLoading">
                {{ creating ? '创建中...' : '创建音乐房间' }}
              </button>

              <button class="drawer-btn" @click="joinFormVisible = !joinFormVisible">
                加入音乐房间
              </button>

              <Transition name="form-expand">
                <div v-if="joinFormVisible" class="form-stack">
                  <input
                    v-model="joinPassword"
                    type="password"
                    class="drawer-input"
                    placeholder="输入房间密码"
                    @keyup.enter="handleJoinRoom"
                  />
                  <button class="drawer-btn primary small" @click="handleJoinRoom" :disabled="roomLoading">
                    确认加入
                  </button>
                </div>
              </Transition>
            </template>

            <!-- in room -->
            <template v-else>
              <div class="info-card">
                <div class="info-row">
                  <span class="info-label">房主ID</span>
                  <span class="info-value">{{ room.userId }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">歌单</span>
                  <span class="info-value">{{ room.musicIdList?.length || 0 }} 首</span>
                </div>
              </div>
              <button class="drawer-btn danger" @click="handleExitRoom">
                退出房间
              </button>
            </template>
          </div>

          <!-- upload music section -->
          <div class="section">
            <div class="section-header" @click="uploadExpanded = !uploadExpanded" style="cursor:pointer">
              <span class="section-icon">⬆</span>
              <span class="section-label">上传音乐</span>
              <span class="section-arrow" :class="{ up: uploadExpanded }">▼</span>
            </div>

            <Transition name="form-expand">
              <div v-if="uploadExpanded" class="form-stack">
                <div class="form-field">
                  <label class="form-label">歌曲名</label>
                  <input
                    v-model="uploadForm.title"
                    class="drawer-input"
                    placeholder="请输入歌曲名"
                  />
                </div>
                <div class="form-field">
                  <label class="form-label">上传者</label>
                  <input
                    v-model="uploadForm.uploaderName"
                    class="drawer-input"
                    placeholder="可选"
                  />
                </div>
                <div class="form-row">
                  <div class="form-field flex-1">
                    <label class="form-label">封面图片</label>
                    <div class="upload-area">
                      <el-upload
                        ref="coverUploadRef"
                        :auto-upload="false"
                        :show-file-list="true"
                        accept="image/*"
                        :on-change="handleUploadCoverChange"
                        :limit="1"
                      >
                        <el-button size="small" class="upload-btn">选择图片</el-button>
                      </el-upload>
                      <img v-if="coverPreviewUrl" :src="coverPreviewUrl" class="cover-preview" />
                    </div>
                  </div>
                  <div class="form-field flex-1">
                    <label class="form-label">音频文件</label>
                    <div class="upload-area">
                      <el-upload
                        ref="uploadRef"
                        :auto-upload="false"
                        :show-file-list="true"
                        accept="audio/*"
                        :on-change="handleUploadFileChange"
                        :limit="1"
                      >
                        <el-button size="small" class="upload-btn">选择文件</el-button>
                      </el-upload>
                    </div>
                  </div>
                </div>
                <button
                  class="drawer-btn primary"
                  :disabled="uploading"
                  @click="handleUploadSubmit"
                >
                  {{ uploading ? '上传中...' : '确认上传' }}
                </button>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped lang="less">
@red: #C3272B;
@red-hover: #D64A4F;
@cream: #FFF8E8;
@cream-dark: #FFEFD5;
@text: #5C3D2E;
@text-muted: #8B7355;

.music-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.music-space {
  flex: 1;
  max-width: 640px;
  width: 100%;

  margin: 80px auto 0 auto;

  padding: 32px 20px;
  color: @text;
  overflow-y: auto;
  box-sizing: border-box;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  background: @cream-dark;
  border: 1px solid @cream-dark;
  color: @text;
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  &:hover { color: @red; background: @cream; }
}

.title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: @text;
}

.count {
  margin-left: auto;
  font-size: 13px;
  color: @text-muted;
}

.now-playing {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: @cream;
  border-radius: 16px;
  border: 1px solid @cream-dark;
  margin-bottom: 24px;
}

.np-cover {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid @red;
  img { width: 100%; height: 100%; object-fit: cover; }
  .np-pause {
    position: absolute; inset: 0;
    background: rgba(255, 248, 232, 0.7);
    display: flex; align-items: center; justify-content: center;
    font-size: 32px; cursor: pointer; color: @red;
  }
}

.np-info {
  display: flex; flex-direction: column; justify-content: center; gap: 8px;
  min-width: 0;
}

.np-title {
  font-size: 18px; font-weight: 600; color: @text;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.np-uploader { font-size: 13px; color: @text-muted; }

.np-controls {
  display: flex; gap: 10px;
}

.np-btn {
  width: 34px; height: 34px;
  border-radius: 50%;
  border: 1px solid @cream-dark;
  background: transparent;
  color: @text-muted;
  font-size: 14px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
  &:hover { background: @cream-dark; color: @red; }
  &.play { background: @red; color: @cream; border: none; }
  &.mode { font-size: 16px; }
}

.list {
  display: flex; flex-direction: column; gap: 4px;
}

.song-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: @cream-dark; }
  &.active { background: @cream; border: 1px solid @cream-dark; }
}

.item-cover {
  width: 40px; height: 40px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.item-info {
  flex: 1; min-width: 0;
}

.item-title {
  font-size: 14px; color: @text;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.item-meta {
  font-size: 12px; color: @text-muted; margin-top: 2px;
}

.playing-indicator {
  font-size: 18px; color: @red;
  animation: pulse 1s ease-in-out infinite;
}

/* ---- progress bar ---- */

.np-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.np-time {
  font-size: 11px;
  color: @text-muted;
  min-width: 32px;
  font-variant-numeric: tabular-nums;
  user-select: none;
}

.np-progress-track {
  flex: 1;
  height: 4px;
  background: @cream-dark;
  border-radius: 2px;
  cursor: pointer;
  position: relative;
}

.np-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, @red, @red-hover);
  border-radius: 2px;
  transition: width 0.1s linear;
  pointer-events: none;
}

.np-progress-thumb {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  background: @red;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
  box-shadow: 0 0 0 2px @cream, 0 0 6px rgba(195, 39, 43, 0.3);

  &.active {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.2);
  }
}

.np-progress-track:hover .np-progress-thumb {
  opacity: 1;
}

/* ---- filter bar ---- */

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.filter-input {
  flex: 1;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid @cream-dark;
  background: white;
  color: @text;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;

  &::placeholder { color: @text-muted; opacity: 0.7; }
  &:focus { border-color: @red; }
}

/* ---- sort dropdown ---- */

.sort-wrapper {
  position: relative;
  flex-shrink: 0;
}

.sort-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 10px;
  border: 1.5px solid @cream-dark;
  background: white;
  color: @text;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;

  &:hover {
    border-color: @red;
  }

  &.active {
    border-color: @red;
    box-shadow: 0 0 0 2px fade(@red, 12%);
  }

  &.selected {
    color: @red;
    border-color: @red;
  }
}

.sort-arrow {
  font-size: 8px;
  color: inherit;
  transition: transform 0.2s;

  &.up {
    transform: rotate(180deg);
  }
}

.sort-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 4px;
  min-width: 120px;
  background: white;
  border-radius: 10px;
  border: 1.5px solid @cream-dark;
  box-shadow: 0 4px 16px rgba(92, 61, 46, 0.12);
  overflow: hidden;
  z-index: 100;
}

.sort-option {
  padding: 8px 14px;
  font-size: 13px;
  color: @text;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: @red;
    color: white;
  }

  &.checked {
    color: @red;
    font-weight: 500;
    background: @cream;
  }
}

.sort-drop-enter-active,
.sort-drop-leave-active {
  transition: all 0.15s ease;
}
.sort-drop-enter-from,
.sort-drop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.list-empty {
  text-align: center;
  padding: 40px 20px;
  color: @text-muted;
  font-size: 14px;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* ---- drawer ---- */

.drawer-toggle {
  position: fixed;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  z-index: 9998;
  width: 40px;
  height: 40px;
  border: 1px solid @cream-dark;
  border-right: none;
  background: @cream;
  color: @text-muted;
  border-radius: 10px 0 0 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s;
  box-shadow: -2px 2px 8px rgba(0,0,0,0.06);

  &:hover {
    background: @cream-dark;
    color: @red;
    width: 44px;
  }

  &.open {
    right: 320px;
    background: @red;
    color: @cream;
    border-color: @red;
    box-shadow: none;
    transform: translateY(-50%);
    &:hover { background: @red-hover; }
  }
}

.toggle-icon {
  line-height: 1;
  transition: transform 0.3s;
}

.drawer-overlay {
  position: fixed;
  inset: 0;
  z-index: 9998;
  background: rgba(92, 61, 46, 0.25);
  backdrop-filter: blur(2px);
}

.drawer-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 320px;
  height: 100vh;
  z-index: 9999;
  background: @cream;
  border-left: 1px solid @cream-dark;
  box-shadow: -8px 0 32px rgba(92, 61, 46, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.drawer-header {
  flex-shrink: 0;
  padding: 24px 24px 16px;
  border-bottom: 1px solid @cream-dark;
}

.drawer-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: @text;
  letter-spacing: 0.3px;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px 24px;
}

/* ---- drawer sections ---- */

.section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section + .section {
  padding-top: 16px;
  border-top: 1px solid @cream-dark;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  user-select: none;
}

.section-icon {
  font-size: 18px;
  line-height: 1;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  color: @text;
}

.section-arrow {
  margin-left: auto;
  font-size: 9px;
  color: @text-muted;
  transition: transform 0.25s;
  &.up { transform: rotate(180deg); }
}

.drawer-btn {
  width: 100%;
  padding: 10px 16px;
  border-radius: 10px;
  border: 1px solid @cream-dark;
  background: transparent;
  color: @text;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;

  &:hover {
    background: @cream-dark;
    color: @red;
  }

  &.primary {
    background: @red;
    color: @cream;
    border: none;
    font-weight: 500;
    &:hover { background: @red-hover; }
    &:disabled { opacity: 0.5; cursor: not-allowed; }
  }

  &.danger {
    border-color: @red;
    color: @red;
    &:hover { background: rgba(195, 39, 43, 0.06); }
  }

  &.small {
    padding: 8px 12px;
    font-size: 13px;
  }
}

.form-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.flex-1 {
  flex: 1;
  min-width: 0;
}

.form-label {
  font-size: 12px;
  font-weight: 500;
  color: @text-muted;
}

.drawer-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid @cream-dark;
  background: white;
  color: @text;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;

  &::placeholder { color: @text-muted; opacity: 0.7; }
  &:focus { border-color: @red; }
}

.info-card {
  background: @cream-dark;
  border-radius: 10px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 13px;
  color: @text-muted;
}

.info-value {
  font-size: 13px;
  font-weight: 600;
  color: @text;
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.upload-btn {
  background: @red !important;
  border-color: @red !important;
  color: @cream !important;
  &:hover {
    background: @red-hover !important;
    border-color: @red-hover !important;
  }
}

.cover-preview {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid @cream-dark;
}

/* ---- transitions ---- */

.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.3s;
}
.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}

.drawer-slide-enter-active,
.drawer-slide-leave-active {
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.drawer-slide-enter-from,
.drawer-slide-leave-to {
  transform: translateX(100%);
}

.form-expand-enter-active,
.form-expand-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.form-expand-enter-from,
.form-expand-leave-to {
  max-height: 0;
  opacity: 0;
}
.form-expand-enter-to,
.form-expand-leave-from {
  max-height: 100px;
  opacity: 1;
}
</style>
