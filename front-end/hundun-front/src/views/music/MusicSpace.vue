<script setup>
import { useMusicStore } from '@/stores/music.js'
import { storeToRefs } from 'pinia'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import defaultCover from '@/assets/img/music/defaultCover.png'
import HundunHeader from '@/views/user/component/common/HundunHeader.vue'
import { loginRouterPush } from '@/utils/login/loginRouterPush.js'

const musicStore = useMusicStore()
const router = useRouter()

const {
  musicList, currentMusic, currentIndex,
  playMode, isPlaying, currentTime, duration
} = storeToRefs(musicStore)
const {
  fetchAllMusic, playSong, togglePlay,
  prevSong, nextSong, togglePlayMode
} = musicStore

const coverSrc = (item) => item?.coverUrl || defaultCover

const formatTime = (t) => {
  if (!t || isNaN(t)) return '0:00'
  const m = Math.floor(t / 60)
  const s = Math.floor(t % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

onMounted(() => {
  if (!musicList.value.length) fetchAllMusic()
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
</style>
