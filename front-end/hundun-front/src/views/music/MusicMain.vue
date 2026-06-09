<script setup>
import { useUserStore } from '@/stores/user.js'
import { useMusicStore } from '@/stores/music.js'
import { storeToRefs } from 'pinia'
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import defaultCover from '@/assets/img/music/defaultCover.png'

const userStore = useUserStore()
const musicStore = useMusicStore()
const router = useRouter()
const route = useRoute()

const inRoom = computed(() => route.path.startsWith('/music/room'))

const {
  musicList, currentMusic, currentIndex,
  playMode, isPlaying, currentTime, duration
} = storeToRefs(musicStore)
const { fetchAllMusic, playSong, togglePlay, prevSong, nextSong, seekTo, togglePlayMode } = musicStore

const expanded = ref(false)

const progressPercent = computed(() => {
  if (!duration.value) return 0
  return (currentTime.value / duration.value) * 100
})

const formatTime = (t) => {
  if (!t || isNaN(t)) return '0:00'
  const m = Math.floor(t / 60)
  const s = Math.floor(t % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

const coverSrc = computed(() => {
  if (currentMusic.value?.coverUrl) return currentMusic.value.coverUrl
  return defaultCover
})

const playModeLabel = computed(() => {
  if (playMode.value === 'single') return '单曲循环'
  if (playMode.value === 'random') return '随机播放'
  return '顺序播放'
})

const playModeIcon = computed(() => {
  if (playMode.value === 'single') return '↺'
  if (playMode.value === 'random') return '↯'
  return '↻'
})

const handleToggleExpand = () => {
  expanded.value = !expanded.value
}

const handleSeek = (e) => {
  const rect = e.currentTarget.getBoundingClientRect()
  const ratio = (e.clientX - rect.left) / rect.width
  seekTo(ratio * duration.value)
}

const goToMusicSpace = () => {
  expanded.value = false
  router.push('/music/main')
}

onMounted(async () => {
  if (userStore.userModelStatus.music !== 1) return
  await fetchAllMusic()
  if (musicList.value.length) {
    const randIdx = Math.floor(Math.random() * musicList.value.length)
    playSong(randIdx)
  }
})
</script>

<template>
  <div
    class="music"
    v-if="userStore.userModelStatus.music === 1 && !inRoom"
  >
    <div class="toggle-btn" @click="handleToggleExpand">
      <img
        v-if="isPlaying"
        src="@/assets/img/music/music_icon.png"
        alt="music"
        class="icon-spin"
      />
      <img
        v-else
        src="@/assets/img/music/music_icon_ban.png"
        alt="music"
      />
    </div>

    <Transition name="panel">
      <div v-if="expanded" class="player-panel">
        <div class="player-body">
          <div class="cover-row">
            <button class="ctrl-btn" @click="prevSong" title="上一首">‹</button>
            <div class="cover-wrap">
              <img :src="coverSrc" alt="cover" class="cover-img" />
              <div v-if="!isPlaying" class="pause-overlay">▶</div>
            </div>
            <button class="ctrl-btn" @click="nextSong" title="下一首">›</button>
          </div>

          <div class="song-title" :title="currentMusic?.title || '未播放'">
            {{ currentMusic?.title || '未播放' }}
          </div>

          <div class="progress-area">
            <span class="time">{{ formatTime(currentTime) }}</span>
            <div class="progress-track" @click="handleSeek">
              <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
              <div class="progress-thumb" :style="{ left: progressPercent + '%' }"></div>
            </div>
            <span class="time">{{ formatTime(duration) }}</span>
          </div>

          <div class="controls-row">
            <button class="play-btn" @click="togglePlay">
              {{ isPlaying ? '⏸' : '▶' }}
            </button>
            <button class="mode-btn" @click="togglePlayMode" :title="playModeLabel">
              {{ playModeIcon }}
            </button>
          </div>
        </div>

        <div class="menu-section">
          <button class="menu-item" @click="goToMusicSpace">
            🎵 进入音乐空间
          </button>
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

.music {
  z-index: 99;
  position: fixed;
  top: 100px;
  right: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.toggle-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: @cream-dark;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(195, 39, 43, 0.3);
  transition: all 0.3s;
  border: 2px solid @cream;

  &:hover {
    box-shadow: 0 0 20px rgba(195, 39, 43, 0.5);
    transform: scale(1.05);
    background: @cream-dark;
  }

  img {
    width: 28px;
    height: 28px;
    display: block;
  }
}

.icon-spin {
  animation: spin 6s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.panel-enter-active,
.panel-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-enter-from,
.panel-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.player-panel {
  margin-top: 12px;
  width: 300px;
  background: @cream;
  border-radius: 16px;
  border: 1px solid @cream-dark;
  box-shadow: 0 8px 32px rgba(92, 61, 46, 0.15);
  overflow: hidden;
}

.player-body {
  padding: 24px 24px 16px;
}

.cover-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
}

.ctrl-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid @cream-dark;
  background: transparent;
  color: @red;
  font-size: 22px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  line-height: 1;

  &:hover {
    background: @cream-dark;
    color: @red-hover;
  }
}

.cover-wrap {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid @red;
  box-shadow: 0 0 16px rgba(195, 39, 43, 0.2);

  .cover-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .pause-overlay {
    position: absolute;
    inset: 0;
    background: rgba(255, 248, 232, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    color: @red;
    font-size: 28px;
    cursor: pointer;
  }
}

.song-title {
  text-align: center;
  color: @text;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-area {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;

  .time {
    font-size: 11px;
    color: @text-muted;
    min-width: 32px;
    font-variant-numeric: tabular-nums;
  }

  .progress-track {
    flex: 1;
    height: 4px;
    background: @cream-dark;
    border-radius: 2px;
    cursor: pointer;
    position: relative;

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, @red, @red-hover);
      border-radius: 2px;
      transition: width 0.1s linear;
    }

    .progress-thumb {
      position: absolute;
      top: 50%;
      width: 10px;
      height: 10px;
      background: @red;
      border-radius: 50%;
      transform: translate(-50%, -50%);
      opacity: 0;
      transition: opacity 0.2s;
      pointer-events: none;
    }

    &:hover .progress-thumb {
      opacity: 1;
    }
  }
}

.controls-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.play-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: @red;
  color: @cream;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: @red-hover;
    transform: scale(1.08);
  }
}

.mode-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid @cream-dark;
  background: transparent;
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
  }
}

.menu-section {
  border-top: 1px solid @cream-dark;
  padding: 8px;
}

.menu-item {
  width: 100%;
  padding: 10px 16px;
  background: transparent;
  border: none;
  color: @text;
  font-size: 14px;
  cursor: pointer;
  text-align: left;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    background: @cream-dark;
    color: @red;
  }
}
</style>
