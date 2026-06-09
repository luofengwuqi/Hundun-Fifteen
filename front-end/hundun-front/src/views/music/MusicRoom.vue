<script setup>
import { useMusicRoomStore } from '@/stores/musicRoom.js'
import { useMusicStore } from '@/stores/music.js'
import { storeToRefs } from 'pinia'
import { onMounted, onUnmounted, computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import HundunHeader from '@/views/user/component/common/HundunHeader.vue'
import { ElMessage } from 'element-plus'
import defaultCover from '@/assets/img/music/defaultCover.png'

const musicRoomStore = useMusicRoomStore()
const musicStore = useMusicStore()
const router = useRouter()
const route = useRoute()

const { room, loading } = storeToRefs(musicRoomStore)
const { exitRoom, addSong, switchSong, deleteSong } = musicRoomStore
const { musicList, currentMusic, currentIndex, isPlaying, playMode, currentTime, duration } = storeToRefs(musicStore)
const { playSong, togglePlay, prevSong, nextSong, togglePlayMode, fetchAllMusic, onEnded, stop, seekTo } = musicStore

const drawerOpen = ref(false)
const activeTab = ref('queue')
const jbSearch = ref('')

const toggleDrawer = () => {
  drawerOpen.value = !drawerOpen.value
}

const coverSrc = (item) => item?.coverUrl || defaultCover

const formatTime = (t) => {
  if (!t || isNaN(t)) return '0:00'
  const m = Math.floor(t / 60)
  const s = Math.floor(t % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

const modeLabel = computed(() => {
  if (playMode.value === 'single') return '单曲循环'
  if (playMode.value === 'random') return '随机播放'
  return '顺序播放'
})

const progressPct = computed(() => {
  if (!duration.value) return 0
  return (currentTime.value / duration.value) * 100
})

const filteredMusicList = computed(() => {
  if (!jbSearch.value.trim()) return musicList.value
  const kw = jbSearch.value.trim().toLowerCase()
  return musicList.value.filter(
    (m) => m.title.toLowerCase().includes(kw) || m.uploaderName?.toLowerCase().includes(kw)
  )
})

const roomCurrentMusic = computed(() => {
  if (!currentMusic.value || !room.value?.musicIdList) return null
  return room.value.musicIdList.includes(currentMusic.value.id) ? currentMusic.value : null
})

const roomMusicList = computed(() => {
  if (!room.value?.musicIdList || !musicList.value.length) return []
  return room.value.musicIdList
    .map((id) => musicList.value.find((m) => m.id === id))
    .filter(Boolean)
})

const autoPlayNext = async () => {
  const queue = room.value?.musicIdList
  if (!queue || !queue.length || !roomCurrentMusic.value) return
  const curId = roomCurrentMusic.value.id
  const idx = queue.indexOf(curId)
  if (idx !== -1 && idx < queue.length - 1) {
    const nextId = queue[idx + 1]
    const musicIdx = musicList.value.findIndex((m) => m.id === nextId)
    if (musicIdx !== -1) {
      await switchSong(nextId)
      playSong(musicIdx)
    }
  }
}

const handleJukeboxClick = async (item) => {
  const musicIdx = musicList.value.findIndex((m) => m.id === item.id)
  if (musicIdx !== -1) {
    await addSong(item.id)
    ElMessage.success(`已添加「${item.title}」到播放队列`)
    if (!isPlaying.value) {
      const queue = room.value?.musicIdList || []
      if (queue.length === 1) {
        await switchSong(item.id)
        playSong(musicIdx)
      }
    }
  }
}

const handleQueueDelete = async (item) => {
  const isCurrent = roomCurrentMusic.value?.id === item.id
  if (isCurrent) togglePlay()
  await deleteSong(item.id)
  if (isCurrent) {
    const queue = room.value?.musicIdList || []
    if (queue.length) {
      const nextId = queue[0]
      const musicIdx = musicList.value.findIndex((m) => m.id === nextId)
      if (musicIdx !== -1) {
        await switchSong(nextId)
        playSong(musicIdx)
      }
    }
  }
}

const playFromQueue = async (queueId) => {
  const musicIdx = musicList.value.findIndex((m) => m.id === queueId)
  if (musicIdx !== -1) {
    await switchSong(queueId)
    playSong(musicIdx)
  }
}

const handlePrev = () => {
  const queue = room.value?.musicIdList
  if (!queue || !queue.length || !roomCurrentMusic.value) return
  const curId = roomCurrentMusic.value.id
  const idx = queue.indexOf(curId)
  if (idx > 0) playFromQueue(queue[idx - 1])
}

const handleNext = () => {
  const queue = room.value?.musicIdList
  if (!queue || !queue.length || !roomCurrentMusic.value) return
  const curId = roomCurrentMusic.value.id
  const idx = queue.indexOf(curId)
  if (idx !== -1 && idx < queue.length - 1) playFromQueue(queue[idx + 1])
}

const handleQueueClick = async (item, idx) => {
  const musicIdx = musicList.value.findIndex((m) => m.id === item.id)
  if (musicIdx !== -1) {
    await switchSong(item.id)
    playSong(musicIdx)
  }
}

const handleExit = async () => {
  await exitRoom()
  ElMessage.success('已退出房间')
  router.push('/music/main')
}

onUnmounted(stop)

onMounted(async () => {
  stop()
  try {
    await musicRoomStore.fetchRoomData()
  } catch {
    ElMessage.error('房间不存在或无权访问')
    router.push('/music/main')
  }
  if (!musicList.value.length) {
    await musicStore.fetchAllMusic()
  }
  onEnded(autoPlayNext)
  if (room.value?.musicIdList?.length) {
    const queue = room.value.musicIdList
    const startId = room.value.currentMusicId || queue[0]
    const musicIdx = musicList.value.findIndex((m) => m.id === startId)
    if (musicIdx !== -1) {
      await switchSong(startId)
      playSong(musicIdx)
    }
  }
})
</script>

<template>
  <div class="room-page">
    <HundunHeader></HundunHeader>

    <div class="player-area">
      <div v-if="roomCurrentMusic" class="player-mid">
        <div class="player-left">
          <div class="player-cover" :class="{ spinning: isPlaying }">
            <div class="vinyl-label"></div>
            <img :src="coverSrc(roomCurrentMusic)" alt="" />
          </div>
        </div>
        <div class="player-right">
          <div class="player-info">
            <h1 class="player-title">{{ roomCurrentMusic.title }}</h1>
            <p class="player-uploader">上传者 {{ roomCurrentMusic.uploaderName }}</p>
          </div>
        </div>
      </div>
      <div v-else class="player-mid">
        <div class="player-placeholder">
          <div class="placeholder-icon">♫</div>
          <div class="placeholder-text">暂无播放</div>
        </div>
      </div>
      <div class="player-bottom">
        <div class="player-progress">
          <span class="progress-time">{{ formatTime(currentTime) }}</span>
          <div class="progress-track-wrap">
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: progressPct + '%' }"></div>
            </div>
            <input
              type="range"
              class="progress-range"
              :min="0"
              :max="duration || 0"
              :value="currentTime"
              @input="seekTo(Number($event.target.value))"
            />
          </div>
          <span class="progress-time">{{ formatTime(duration) }}</span>
        </div>
        <div class="player-controls">
          <button class="ctrl-btn ctrl-placeholder" disabled aria-hidden="true"></button>
          <button class="ctrl-btn" @click="handlePrev" title="上一首">⏮</button>
          <button class="ctrl-btn ctrl-play" @click="togglePlay">{{ isPlaying ? '⏸' : '▶' }}</button>
          <button class="ctrl-btn" @click="handleNext" title="下一首">⏭</button>
          <span class="ctrl-divider"></span>
          <button class="ctrl-btn ctrl-mode" @click="togglePlayMode" :title="modeLabel">
            {{ playMode === 'single' ? '↺' : playMode === 'random' ? '↯' : '↻' }}
          </button>
        </div>
      </div>
    </div>

    <button class="drawer-toggle" :class="{ open: drawerOpen }" @click="toggleDrawer">
      <span class="toggle-icon">{{ drawerOpen ? '✕' : '☰' }}</span>
    </button>

    <Transition name="drawer-fade">
      <div v-if="drawerOpen" class="drawer-overlay" @click="drawerOpen = false"></div>
    </Transition>

    <Transition name="drawer-slide">
      <div v-if="drawerOpen" class="drawer-panel">
        <div class="drawer-header">
          <h2 class="drawer-title">房间详情</h2>
        </div>

        <div v-if="room" class="drawer-body">
          <div class="room-card">
            <div class="room-info-row">
              <span class="info-label">房主ID</span>
              <span class="info-value">{{ room.userId }}</span>
            </div>
            <div class="room-info-row">
              <span class="info-label">歌单数量</span>
              <span class="info-value">{{ room.musicIdList?.length || 0 }} 首</span>
            </div>
            <div class="room-info-row" v-if="room.currentMusicId">
              <span class="info-label">当前播放</span>
              <span class="info-value">#{{ room.currentMusicId }}</span>
            </div>
          </div>

          <div class="tab-section">
            <div class="tab-bar">
              <button
                class="tab-btn"
                :class="{ active: activeTab === 'queue' }"
                @click="activeTab = 'queue'"
              >播放队列</button>
              <button
                class="tab-btn"
                :class="{ active: activeTab === 'jukebox' }"
                @click="activeTab = 'jukebox'"
              >点歌台</button>
            </div>

            <div v-show="activeTab === 'queue'" class="tab-content">
              <div v-if="roomMusicList.length" class="queue-list">
                <div
                  v-for="(item, idx) in roomMusicList"
                  :key="item.id"
                  class="queue-item"
                  :class="{ active: item.id === roomCurrentMusic?.id }"
                  @click="handleQueueClick(item, idx)"
                >
                  <span class="queue-idx">{{ idx + 1 }}</span>
                  <div class="queue-info">
                    <div class="queue-title">{{ item.title }}</div>
                    <div class="queue-meta">{{ item.uploaderName }}</div>
                  </div>
                  <span v-if="item.id === roomCurrentMusic?.id" class="queue-badge">▶</span>
                  <button class="queue-del" @click.stop="handleQueueDelete(item)" title="移除">✕</button>
                </div>
              </div>
              <div v-else class="tab-empty">歌单是空的</div>
            </div>

            <div v-show="activeTab === 'jukebox'" class="tab-content">
              <div class="jb-search">
                <input v-model="jbSearch" class="jb-search-input" placeholder="搜索歌曲或上传者..." />
              </div>
              <div v-if="filteredMusicList.length" class="jb-list">
                <div
                  v-for="(item, idx) in filteredMusicList"
                  :key="item.id"
                  class="jb-item"
                  :class="{ active: item.id === roomCurrentMusic?.id }"
                  @click="handleJukeboxClick(item)"
                >
                  <img :src="coverSrc(item)" alt="" class="jb-item-cover" />
                  <div class="jb-item-info">
                    <div class="jb-item-title">{{ item.title }}</div>
                    <div class="jb-item-meta">{{ item.uploaderName }} · {{ formatTime(item.duration) }}</div>
                  </div>
                  <span v-if="item.id === roomCurrentMusic?.id && isPlaying" class="jb-playing">♫</span>
                </div>
              </div>
              <div v-else class="tab-empty">没有匹配的歌曲</div>
            </div>
          </div>
        </div>

        <div class="drawer-footer">
          <button class="exit-btn" @click="handleExit">退出房间</button>
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

.room-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.player-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 0;
  background:
    radial-gradient(ellipse 80% 60% at 30% 40%, rgba(195,39,43,0.04) 0%, transparent 70%),
    @cream;
}

.player-mid {
  flex: 1;
  display: flex;
  width: 100%;
  padding: 0 64px;
  box-sizing: border-box;
  min-height: 0;
}

.player-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 150px;
}

.player-right {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.player-info {
  text-align: center;
  margin-bottom: 160px;
}

.player-bottom {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 28px;
  width: 100%;
  padding: 0 64px 32px;
  box-sizing: border-box;
}

.player-inner {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 40px;
  width: 100%;
  max-width: 800px;
}

.player-top {
  display: flex;
  align-items: center;
  gap: 40px;
}

.player-cover {
  position: relative;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 5px solid @red;
  box-shadow:
    0 0 0 8px @cream,
    0 0 0 11px fade(@red, 18%),
    0 12px 48px rgba(195,39,43,0.2);

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 50%;
    z-index: 1;
    pointer-events: none;
    box-shadow:
      inset 0 0 0 30px rgba(0,0,0,0.05),
      inset 0 0 0 70px rgba(0,0,0,0.03),
      inset 0 0 0 110px rgba(0,0,0,0.025),
      inset 0 0 0 140px rgba(0,0,0,0.02);
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .vinyl-label {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: @cream;
    border: 3px solid fade(@red, 25%);
    z-index: 3;
    pointer-events: none;
    box-shadow: inset 0 2px 8px rgba(0,0,0,0.08);

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 14px;
      height: 14px;
      border-radius: 50%;
      background: @red;
    }
  }

  &.spinning img {
    animation: spin 6s linear infinite;
  }

  &.spinning::before {
    animation: spin 6s linear infinite;
  }
}

.player-title {
  margin: 0 0 6px;
  font-size: 30px;
  font-weight: 800;
  color: @text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: -0.3px;
  line-height: 1.2;
}

.player-uploader {
  margin: 0;
  font-size: 14px;
  color: @text-muted;
  letter-spacing: 0.2px;
}

.player-bottom {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.player-progress {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.progress-time {
  font-size: 13px;
  color: @text-muted;
  white-space: nowrap;
  min-width: 40px;
  text-align: center;
  font-variant-numeric: tabular-nums;
  user-select: none;
}

.progress-track-wrap {
  flex: 1;
  position: relative;
  height: 6px;
}

.progress-track {
  position: absolute;
  inset: 0;
  border-radius: 3px;
  background: @cream-dark;
  overflow: hidden;
  pointer-events: none;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  background: @red;
  transition: width 0.15s linear;
}

.progress-range {
  position: absolute;
  inset: -8px 0;
  width: 100%;
  margin: 0;
  -webkit-appearance: none;
  appearance: none;
  background: transparent;
  cursor: pointer;
  z-index: 1;

  &::-webkit-slider-thumb {
    -webkit-appearance: none;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: @red;
    border: 3px solid @cream;
    box-shadow: 0 0 0 2px fade(@red, 25%), 0 2px 8px rgba(0,0,0,0.12);
    cursor: pointer;
    transition: transform 0.15s, box-shadow 0.15s;
  }

  &::-webkit-slider-thumb:hover {
    transform: scale(1.15);
    box-shadow: 0 0 0 4px fade(@red, 25%), 0 2px 12px rgba(0,0,0,0.18);
  }

  &::-moz-range-thumb {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: @red;
    border: 3px solid @cream;
    cursor: pointer;
  }
}

.player-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.ctrl-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid @cream-dark;
  background: @cream;
  color: @text-muted;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: @cream-dark;
    color: @red;
    border-color: fade(@red, 20%);
  }

  &.ctrl-play {
    width: 56px;
    height: 56px;
    background: @red;
    color: @cream;
    border: none;
    font-size: 22px;
    box-shadow: 0 4px 16px rgba(195,39,43,0.35);

    &:hover {
      background: @red-hover;
      transform: scale(1.06);
      box-shadow: 0 6px 24px rgba(195,39,43,0.4);
    }
  }

  &.ctrl-mode {
    font-size: 20px;
    opacity: 0.55;

    &:hover {
      opacity: 1;
      color: @text;
    }
  }

  &.ctrl-placeholder {
    pointer-events: none;
    opacity: 0.55;
    font-size: 20px;
  }
}

.ctrl-divider {
  width: 1px;
  height: 24px;
  background: @cream-dark;
  margin: 0 4px;
  flex-shrink: 0;
}

.player-placeholder {
  text-align: center;
  margin: auto;
}

.placeholder-icon {
  font-size: 48px;
  color: @red;
  margin-bottom: 16px;
  animation: pulse 2s ease-in-out infinite;
}

.placeholder-text {
  font-size: 16px;
  color: @text-muted;
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
  padding: 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.drawer-footer {
  flex-shrink: 0;
  padding: 12px 24px 24px;
  border-top: 1px solid @cream-dark;
}

/* ---- room card in drawer ---- */

.room-card {
  background: white;
  border-radius: 12px;
  border: 1px solid @cream-dark;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.room-info-row {
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

/* ---- tabs ---- */

.tab-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tab-bar {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid @cream-dark;
  background: @cream-dark;
}

.tab-btn {
  flex: 1;
  padding: 7px 0;
  border: none;
  background: transparent;
  color: @text-muted;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 7px;

  &.active {
    background: @cream;
    color: @red;
    box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  }

  &:not(.active):hover {
    color: @text;
  }
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tab-empty {
  text-align: center;
  padding: 24px;
  color: @text-muted;
  font-size: 13px;
}

.queue-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.queue-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: default;
  transition: background 0.2s;
  &:hover { background: @cream-dark; }
  &.active {
    background: @cream;
    border: 1px solid @cream-dark;
  }
}

.queue-idx {
  width: 20px;
  text-align: center;
  font-size: 12px;
  color: @text-muted;
  flex-shrink: 0;
}

.queue-info {
  flex: 1;
  min-width: 0;
}

.queue-title {
  font-size: 13px;
  color: @text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.queue-meta {
  font-size: 11px;
  color: @text-muted;
  margin-top: 1px;
}

.queue-badge {
  font-size: 12px;
  color: @red;
  flex-shrink: 0;
  animation: pulse 1s ease-in-out infinite;
}

.queue-del {
  width: 20px;
  height: 20px;
  border: none;
  background: transparent;
  color: @text-muted;
  font-size: 11px;
  cursor: pointer;
  border-radius: 4px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  opacity: 0;
  .queue-item:hover & {
    opacity: 1;
  }
  &:hover {
    background: @cream-dark;
    color: @red;
  }
}

.jb-search {
  flex-shrink: 0;
}

.jb-search-input {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid @cream-dark;
  background: white;
  color: @text;
  font-size: 12px;
  outline: none;
  transition: border-color 0.2s;
  &::placeholder { color: @text-muted; opacity: 0.7; }
  &:focus { border-color: @red; }
}

.jb-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 220px;
  overflow-y: auto;
}

.jb-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: @cream-dark; }
  &.active { background: @cream; border: 1px solid @cream-dark; }
}

.jb-item-cover {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

.jb-item-info {
  flex: 1;
  min-width: 0;
}

.jb-item-title {
  font-size: 12px;
  color: @text;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.jb-item-meta {
  font-size: 11px;
  color: @text-muted;
  margin-top: 1px;
}

.jb-playing {
  font-size: 14px;
  color: @red;
  animation: pulse 1s ease-in-out infinite;
}



@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.exit-btn {
  width: 100%;
  padding: 10px 16px;
  border-radius: 10px;
  border: 1px solid @red;
  background: transparent;
  color: @red;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: rgba(195, 39, 43, 0.06); }
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
</style>
