<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user.js'
import GuidePage1 from './GuidePage1.vue'
import GuidePage2 from './GuidePage2.vue'
import GuideFinal from './GuideFinal.vue'

const emit = defineEmits(['close'])
const userStore = useUserStore()

const currentPage = ref(1)
const totalPages = 3

const moduleSelections = ref({
  health: 1,
  finance: 1,
  knowledge: 1,
  memo: 1,
  music: 1
})

const canNext = computed(() => {
  if (currentPage.value === 2) {
    return moduleSelections.value.health === 1 && moduleSelections.value.finance === 1
  }
  return true
})

const handleNext = async () => {
  if (currentPage.value === 2) {
    try {
      await userStore.batchUpdateModules({
        health: moduleSelections.value.health,
        finance: moduleSelections.value.finance,
        knowledge: moduleSelections.value.knowledge,
        memo: moduleSelections.value.memo,
        music: moduleSelections.value.music
      })
    } catch {
      ElMessage.error('保存模块设置失败')
      return
    }
  }
  if (currentPage.value < totalPages) {
    currentPage.value++
  }
}

const handlePrev = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

const handleSkip = async () => {
  try {
    await userStore.setNewUserStatus(0)
  } catch {
    // ignore
  }
  emit('close')
}

const handleComplete = async () => {
  try {
    await userStore.setNewUserStatus(0)
  } catch {
    // ignore
  }
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div class="guide-overlay">
      <div class="guide-card">
        <div class="guide-body">
          <div class="page-indicator">
            <span
              v-for="i in totalPages"
              :key="i"
              class="dot"
              :class="{ active: currentPage === i }"
            ></span>
          </div>

          <div class="page-content">
            <Transition name="page" mode="out-in">
              <GuidePage1
                v-if="currentPage === 1"
                :key="1"
              />
              <GuidePage2
                v-else-if="currentPage === 2"
                :key="2"
                v-model="moduleSelections"
              />
              <GuideFinal
                v-else-if="currentPage === 3"
                :key="3"
              />
            </Transition>
          </div>
        </div>

        <div class="guide-footer">
          <div class="footer-left">
            <button
              v-if="currentPage === 1"
              class="btn-text"
              @click="handleSkip"
            >
              跳过指引
            </button>
            <button
              v-else
              class="btn-text"
              @click="handlePrev"
            >
              上一页
            </button>
          </div>
          <div class="footer-right">
            <button
              v-if="currentPage < totalPages"
              class="btn-primary"
              :class="{ disabled: !canNext }"
              :disabled="!canNext"
              @click="handleNext"
            >
              下一页
            </button>
            <button
              v-else
              class="btn-primary"
              @click="handleComplete"
            >
              完成
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="less">
@red: #C3272B;
@red-hover: #D64A4F;
@cream: #FFF8E8;
@cream-dark: #FFEFD5;
@text: #5C3D2E;
@text-muted: #8B7355;

.guide-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(2px);
}

.guide-card {
  width: 50vw;
  height: 80vh;
  max-width: 700px;
  max-height: 620px;
  background: @cream;
  border-radius: 24px;
  box-shadow: 0 16px 48px rgba(92, 61, 46, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.guide-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 40px 48px 0;
  position: relative;
}

.page-indicator {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-bottom: 24px;

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ddd;
    transition: all 0.3s;

    &.active {
      background: @red;
      width: 24px;
      border-radius: 4px;
    }
  }
}

.page-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-enter-active,
.page-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.page-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.page-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.guide-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 48px 24px;
}

.footer-left,
.footer-right {
  display: flex;
  gap: 12px;
}

.btn-text {
  background: none;
  border: none;
  color: @text-muted;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    color: @text;
    background: rgba(92, 61, 46, 0.06);
  }
}

.btn-primary {
  background: @red;
  color: #fff;
  border: none;
  padding: 8px 28px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: @red-hover;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
