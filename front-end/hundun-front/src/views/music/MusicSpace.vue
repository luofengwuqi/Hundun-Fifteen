<script setup>
import { useMusicStore } from '@/stores/music.js'
import { useMusicRoomStore } from '@/stores/musicRoom.js'
import { useUserStore } from '@/stores/user.js'
import { storeToRefs } from 'pinia'
import { onMounted, ref, watch } from 'vue'
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
  prevSong, nextSong, togglePlayMode
} = musicStore

const { room, loading: roomLoading } = storeToRefs(musicRoomStore)
const { fetchRoomData, createRoom, joinRoom, exitRoom } = musicRoomStore

const drawerOpen = ref(false)
const joinFormVisible = ref(false)
const joinPassword = ref('')
const creating = ref(false)

const ownerId = route.params.ownerId

const coverSrc = (item) => item?.coverUrl || defaultCover

const formatTime = (t) => {
  if (!t || isNaN(t)) return '0:00'
  const m = Math.floor(t / 60)
  const s = Math.floor(t % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

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
        <button class="back-btn" @click="loginRouterPush">← 返回</button>
        <h1 class="title">🎵 音乐空间</h1>
        <span class="count">{{ musicList.length }} 首</span>
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
        </div>
      </div>

      <div class="list">
        <div
          v-for="(item, idx) in musicList"
          :key="item.id"
          class="song-item"
          :class="{ active: idx === currentIndex }"
          @click="playSong(idx)"
        >
          <img :src="coverSrc(item)" alt="" class="item-cover" />
          <div class="item-info">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-meta">{{ item.uploaderName }} · {{ formatTime(item.duration) }}</div>
          </div>
          <div v-if="idx === currentIndex && isPlaying" class="playing-indicator">♫</div>
        </div>
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
          <div class="room-section">
            <div class="room-section-header">
              <span class="room-icon">🎶</span>
              <span class="room-label">音乐房间</span>
            </div>

            <!-- not in room -->
            <template v-if="!room">
              <button class="room-btn" @click="handleCreateRoom" :disabled="creating || roomLoading">
                {{ creating ? '创建中...' : '创建音乐房间' }}
              </button>

              <button class="room-btn" @click="joinFormVisible = !joinFormVisible">
                加入音乐房间
              </button>

              <Transition name="form-expand">
                <div v-if="joinFormVisible" class="room-form">
                  <input
                    v-model="joinPassword"
                    type="password"
                    class="room-input"
                    placeholder="输入房间密码"
                    @keyup.enter="handleJoinRoom"
                  />
                  <button class="room-btn primary small" @click="handleJoinRoom" :disabled="roomLoading">
                    确认加入
                  </button>
                </div>
              </Transition>
            </template>

            <!-- in room -->
            <template v-else>
              <div class="room-info">
                <div class="room-info-row">
                  <span class="room-info-label">房主ID</span>
                  <span class="room-info-value">{{ room.userId }}</span>
                </div>
                <div class="room-info-row">
                  <span class="room-info-label">歌单</span>
                  <span class="room-info-value">{{ room.musicIdList?.length || 0 }} 首</span>
                </div>
              </div>
              <button class="room-btn danger" @click="handleExitRoom">
                退出房间
              </button>
            </template>
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

/* ---- room section ---- */

.room-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.room-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.room-icon {
  font-size: 20px;
}

.room-label {
  font-size: 15px;
  font-weight: 600;
  color: @text;
}

.room-btn {
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

.room-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

.room-input {
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

.room-info {
  background: @cream-dark;
  border-radius: 10px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.room-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.room-info-label {
  font-size: 13px;
  color: @text-muted;
}

.room-info-value {
  font-size: 13px;
  font-weight: 600;
  color: @text;
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
