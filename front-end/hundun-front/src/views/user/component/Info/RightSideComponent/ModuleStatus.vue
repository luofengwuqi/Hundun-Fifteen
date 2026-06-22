<script setup>
import { useUserStore } from '@/stores/user.js'
import { storeToRefs } from 'pinia'
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

import healthImage from '@/assets/img/user/home/health.png'
import financeImage from '@/assets/img/user/home/finance.png'
import knowledgeImage from '@/assets/img/user/home/knowledge.png'
import memoImage from '@/assets/img/user/home/memo.png'
import musicImage from '@/assets/img/music/music_icon.png'

const userStore = useUserStore()
const { userModelStatus } = storeToRefs(userStore)
const { initUserModelStatus, enableCategory, disableCategory } = userStore

const toggling = ref(null)

const moduleDefs = [
  { key: 'health', label: '健康', icon: healthImage, locked: true },
  { key: 'finance', label: '财务', icon: financeImage, locked: true },
  { key: 'knowledge', label: '知识与社区', icon: knowledgeImage },
  { key: 'memo', label: '备忘', icon: memoImage },
  { key: 'music', label: '音乐', icon: musicImage },
]

const enabledModules = computed(() =>
  moduleDefs.filter((m) => userModelStatus.value[m.key] === 1)
)

const disabledModules = computed(() =>
  moduleDefs.filter((m) => userModelStatus.value[m.key] !== 1)
)

const handleToggle = async (mod) => {
  if (toggling.value || mod.locked) return
  toggling.value = mod.key

  try {
    if (userModelStatus.value[mod.key] === 1) {
      await disableCategory(mod.key)
    } else {
      await enableCategory(mod.key)
    }
  } catch {
    ElMessage.error('操作失败，请重试')
  } finally {
    toggling.value = null
  }
}

onMounted(async () => {
  if (!userModelStatus.value || !Object.keys(userModelStatus.value).length) {
    await initUserModelStatus()
  }
})
</script>

<template>
  <div class="module-status">
    <div class="page-header">
      <div class="header-accent"></div>
      <div class="header-content">
        <h2 class="header-title">模块开关</h2>
        <p class="header-desc">点击模块可在已启用与未启用之间切换</p>
      </div>
    </div>

    <div class="section">
      <div class="section-head">
        <span class="section-badge enabled-badge">已启用</span>
        <span class="section-count">{{ enabledModules.length }}</span>
      </div>

      <div class="scroll-wrap">
        <div class="card-track">
          <TransitionGroup name="card">
            <div
              v-for="mod in enabledModules"
              :key="mod.key"
              class="module-card enabled"
              :class="{
                toggling: toggling === mod.key,
                locked: mod.locked
              }"
              @click="handleToggle(mod)"
            >
              <div class="card-left">
                <div class="card-icon">
                  <img :src="mod.icon" :alt="mod.label" />
                </div>
                <span class="card-label">{{ mod.label }}</span>
              </div>
              <div class="card-right">
                <span v-if="mod.locked" class="card-lock-hint">固有</span>
                <span v-else class="card-vert-hint" v-html="'点击关闭'"></span>
              </div>
            </div>
          </TransitionGroup>
          <div v-if="!enabledModules.length" class="empty-hint">暂未启用任何模块</div>
        </div>
      </div>
    </div>

    <div class="section-divider">
      <span class="divider-dot"></span>
      <span class="divider-dot"></span>
      <span class="divider-dot"></span>
    </div>

    <div class="section">
      <div class="section-head">
        <span class="section-badge disabled-badge">未启用</span>
        <span class="section-count">{{ disabledModules.length }}</span>
      </div>

      <div class="scroll-wrap">
        <div class="card-track">
          <TransitionGroup name="card">
            <div
              v-for="mod in disabledModules"
              :key="mod.key"
              class="module-card disabled"
              :class="{ toggling: toggling === mod.key }"
              @click="handleToggle(mod)"
            >
              <div class="card-left">
                <div class="card-icon">
                  <img :src="mod.icon" :alt="mod.label" />
                </div>
                <span class="card-label">{{ mod.label }}</span>
              </div>
              <div class="card-right">
                <span class="card-vert-hint" v-html="'点击启用'"></span>
              </div>
            </div>
          </TransitionGroup>
          <div v-if="!disabledModules.length" class="empty-hint">所有模块已启用</div>
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

.module-status {
  padding: 28px 32px;
}

/* ---- page header ---- */

.page-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 32px;
}

.header-accent {
  width: 4px;
  height: 44px;
  border-radius: 2px;
  background: linear-gradient(180deg, @red, @red-hover);
  flex-shrink: 0;
  margin-top: 2px;
}

.header-content {
  flex: 1;
  min-width: 0;
}

.header-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: @text;
  letter-spacing: 1px;
}

.header-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: @text-muted;
}

/* ---- section ---- */

.section {
  margin-bottom: 8px;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-badge {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 2px;
  padding: 4px 14px;
  border-radius: 6px;

  &.enabled-badge {
    color: @red;
    background: fade(@red, 8%);
  }

  &.disabled-badge {
    color: @text-muted;
    background: fade(@text-muted, 10%);
  }
}

.section-count {
  font-size: 13px;
  color: @text-muted;
  font-weight: 500;
}

/* ---- scroll area ---- */

.scroll-wrap {
  overflow-x: auto;
  overflow-y: visible;
  padding-bottom: 8px;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: @cream-dark;
    border-radius: 2px;
  }
}

.card-track {
  display: flex;
  gap: 12px;
  min-width: max-content;
  padding: 4px 0;
}

/* ---- module card ---- */

.module-card {
  position: relative;
  width: 180px;
  flex-shrink: 0;
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;

  &.enabled {
    background: linear-gradient(145deg, @cream, @cream-dark);
    border: 1.5px solid fade(@red, 15%);
    box-shadow:
      0 4px 16px fade(@red, 8%),
      inset 0 1px 0 rgba(255, 255, 255, 0.6);

    .card-icon {
      background: linear-gradient(135deg, @cream-dark, @cream);
      border-color: fade(@red, 20%);
    }

    .card-label {
      color: @text;
    }

    &:hover:not(.locked) {
      transform: translateY(-3px);
      box-shadow:
        0 8px 28px fade(@red, 14%),
        inset 0 1px 0 rgba(255, 255, 255, 0.6);
      border-color: fade(@red, 30%);

      .card-vert-hint {
        opacity: 1;
      }
    }

    &:active:not(.locked) {
      transform: translateY(-1px) scale(0.97);
    }

    &.locked {
      cursor: default;
      border-color: fade(@gold, 20%);
      box-shadow:
        0 4px 16px fade(@gold, 8%),
        inset 0 1px 0 rgba(255, 255, 255, 0.6);
    }
  }

  &.disabled {
    background: linear-gradient(145deg, #fafafa, #f5f3f1);
    border: 1.5px solid #e8e4e0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);

    .card-icon {
      background: #f0eeec;
      border-color: #e0dcd8;
      filter: grayscale(1);
      opacity: 0.5;
    }

    .card-icon img {
      filter: grayscale(1);
      opacity: 0.5;
    }

    .card-label {
      color: @text-muted;
    }

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
      border-color: @cream-dark;
      background: linear-gradient(145deg, @cream, @cream-dark);

      .card-icon {
        filter: grayscale(0);
        opacity: 1;
        border-color: fade(@red, 20%);
        background: linear-gradient(135deg, @cream-dark, @cream);
      }

      .card-icon img {
        filter: grayscale(0);
        opacity: 1;
      }

      .card-label {
        color: @text;
      }

      .card-vert-hint {
        opacity: 1;
        color: @red;
      }
    }

    &:active {
      transform: translateY(-1px) scale(0.97);
    }
  }

  &.toggling {
    pointer-events: none;
    transform: scale(0.92) !important;
    opacity: 0.4;
  }
}

@gold: #B8860B;

.card-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.card-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: 1.5px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  flex-shrink: 0;

  img {
    width: 26px;
    height: 26px;
    object-fit: contain;
    transition: all 0.3s;
  }
}

.card-label {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  transition: color 0.3s;
}

.card-vert-hint {
  font-size: 11px;
  font-weight: 500;
  color: @text-muted;
  line-height: 1.5;
  text-align: center;
  letter-spacing: 0.5px;
  opacity: 0;
  transition: opacity 0.25s, color 0.25s;
  writing-mode: vertical-lr;
  padding: 2px 0;
}

.module-card:not(.locked):hover .card-vert-hint {
  opacity: 1;
}

.card-lock-hint {
  font-size: 11px;
  font-weight: 500;
  color: @gold;
  opacity: 0.7;
}

/* ---- transitions ---- */

.card-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.card-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
}

.card-enter-from {
  opacity: 0;
  transform: scale(0.7) translateY(20px);
}

.card-leave-to {
  opacity: 0;
  transform: scale(0.7) translateY(-20px);
}

.card-move {
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* ---- empty ---- */

.empty-hint {
  font-size: 13px;
  color: @text-muted;
  padding: 24px 0;
  text-align: center;
  width: 100%;
  min-width: 200px;
}

/* ---- section divider ---- */

.section-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 20px 0 24px;
}

.divider-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: @cream-dark;

  &:nth-child(2) {
    width: 6px;
    height: 6px;
    background: @red;
    opacity: 0.4;
  }
}
</style>
