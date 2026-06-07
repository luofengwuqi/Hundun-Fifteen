<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import DiaryInput from './form/DiaryInput.vue'
import WhatAteToday from './form/WhatAteToday.vue'
import MealCost from './form/MealCost.vue'
import FeatureDisabled from './form/FeatureDisabled.vue'
import MemoCommon from './form/MemoCommon.vue'
import MemoImportant from './form/MemoImportant.vue'
import Payment from './form/Payment.vue'
import SleepHealth from './form/SleepHealth.vue'
import MoodHealth from './form/MoodHealth.vue'
import PlaceWhere from './form/PlaceWhere.vue'

/** 与 CSS 中 --insert-panel-duration 保持一致 */
const PANEL_TRANSITION_MS = 360

const userStore = useUserStore()
onMounted(async () => {
  if (!userStore.userModelStatus || !userStore.userModelStatus.health) {
    await userStore.initUserModelStatus()
  }
})

const props = defineProps({
  selectedActions: {
    type: Array,
    default: () => []
  },
  diaryMode: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['hide-component', 'update:diaryMode'])

const isModulesEnabled = computed(() => {
  const status = userStore.userModelStatus
  return status && status.health === 1 && status.finance === 1
})

const isHealthEnabled = computed(() => {
  const status = userStore.userModelStatus
  return status && status.health === 1
})

const hiddenComponents = ref(new Set())

const handleHideComponent = (componentName) => {
  if (hiddenComponents.value.has(componentName)) return
  hiddenComponents.value = new Set([...hiddenComponents.value, componentName])
  setTimeout(() => {
    emit('hide-component', componentName)
  }, PANEL_TRANSITION_MS)
}

const componentsToShow = computed(() => {
  const components = []
  const hasSelectedEat = props.selectedActions.includes('吃了什么')

  if (hasSelectedEat) {
    if (!isModulesEnabled.value) {
      if (!hiddenComponents.value.has('FeatureDisabled')) {
        components.push({
          name: 'FeatureDisabled',
          component: FeatureDisabled,
          props: {
            title: '功能未启用 (灬º﹃º灬)',
            message: '使用当前组件请先启用财务模块和健康模块',
            icon: '🔒'
          }
        })
      }
    } else {
      if (!hiddenComponents.value.has('WhatAteToday')) {
        components.push({
          name: 'WhatAteToday',
          component: WhatAteToday,
          onHide: () => handleHideComponent('WhatAteToday')
        })
      }
      if (!hiddenComponents.value.has('MealCost')) {
        components.push({
          name: 'MealCost',
          component: MealCost,
          onHide: () => handleHideComponent('MealCost')
        })
      }
    }
  }

  if (props.selectedActions.includes('去了哪里')) {
    if (!isModulesEnabled.value) {
      if (!hiddenComponents.value.has('PlaceFeatureDisabled')) {
        components.push({
          name: 'PlaceFeatureDisabled',
          component: FeatureDisabled,
          props: {
            title: '功能未启用 (灬º﹃º灬)',
            message: '使用当前组件请先启用财务模块和健康模块',
            icon: '🔒'
          }
        })
      }
    } else if (!hiddenComponents.value.has('PlaceWhere')) {
      components.push({
        name: 'PlaceWhere',
        component: PlaceWhere,
        onHide: () => handleHideComponent('PlaceWhere')
      })
    }
  }
  if (props.selectedActions.includes('买了点啥')) {
    if (!hiddenComponents.value.has('Payment')) {
      components.push({
        name: 'Payment',
        component: Payment,
        onHide: () => handleHideComponent('Payment')
      })
    }
  }
  if (props.selectedActions.includes('睡得咋样')) {
    if (!isHealthEnabled.value) {
      if (!hiddenComponents.value.has('SleepFeatureDisabled')) {
        components.push({
          name: 'SleepFeatureDisabled',
          component: FeatureDisabled,
          props: {
            title: '功能未启用 (灬º﹃º灬)',
            message: '记录睡眠请先启用健康模块',
            icon: '🔒'
          }
        })
      }
    } else if (!hiddenComponents.value.has('SleepHealth')) {
      components.push({
        name: 'SleepHealth',
        component: SleepHealth,
        onHide: () => handleHideComponent('SleepHealth')
      })
    }
  }
  if (props.selectedActions.includes('心情如何')) {
    if (!isHealthEnabled.value) {
      if (!hiddenComponents.value.has('MoodFeatureDisabled')) {
        components.push({
          name: 'MoodFeatureDisabled',
          component: FeatureDisabled,
          props: {
            title: '功能未启用 (灬º﹃º灬)',
            message: '记录心情请先启用健康模块',
            icon: '🔒'
          }
        })
      }
    } else if (!hiddenComponents.value.has('MoodHealth')) {
      components.push({
        name: 'MoodHealth',
        component: MoodHealth,
        onHide: () => handleHideComponent('MoodHealth')
      })
    }
  }
  if (props.selectedActions.includes('今日记录')) {
    console.log('TODO: 添加今日记录的组件')
  }
  if (props.selectedActions.includes('整点计划')) {
    if (!hiddenComponents.value.has('MemoCommon')) {
      components.push({
        name: 'MemoCommon',
        component: MemoCommon,
        onHide: () => handleHideComponent('MemoCommon')
      })
    }
  }
  if (props.selectedActions.includes('重要备忘')) {
    if (!hiddenComponents.value.has('MemoImportant')) {
      components.push({
        name: 'MemoImportant',
        component: MemoImportant,
        onHide: () => handleHideComponent('MemoImportant')
      })
    }
  }

  return components
})

const hasPendingSelection = computed(
  () => props.selectedActions.length > 0 && componentsToShow.value.length === 0
)

watch(
  () => props.selectedActions,
  () => {
    hiddenComponents.value = new Set()
  },
  { deep: true }
)

/** 离开动画期间锁定尺寸与位置，避免 absolute 脱离文档流后卡片飞到左上角 */
const onPanelBeforeLeave = (el) => {
  const rect = el.getBoundingClientRect()
  const parent = el.parentElement
  if (!parent) return
  const parentRect = parent.getBoundingClientRect()
  el.style.width = `${rect.width}px`
  el.style.height = `${rect.height}px`
  el.style.left = `${rect.left - parentRect.left + parent.scrollLeft}px`
  el.style.top = `${rect.top - parentRect.top + parent.scrollTop}px`
  el.style.margin = '0'
}

const onPanelAfterLeave = (el) => {
  el.style.width = ''
  el.style.height = ''
  el.style.left = ''
  el.style.top = ''
  el.style.margin = ''
}
</script>

<template>
  <div class="right-side-container">
    <div class="workspace-title" v-if="!diaryMode">
      <h2>工作台</h2>
    </div>

    <!-- 日记 AI 模式 -->
    <DiaryInput
      v-if="diaryMode"
      class="diary-input-full"
      @back="$emit('update:diaryMode', false)"
    />

    <Transition v-else name="empty-fade" mode="out-in">
      <div v-if="componentsToShow.length === 0" key="empty" class="empty-state">
        <p class="empty-text">
          {{
            hasPendingSelection
              ? '所选功能正在开发中，敬请期待 (・ω< )'
              : '请选择左侧的操作项 (・ω< )'
          }}
        </p>
      </div>
      <TransitionGroup
        v-else
        key="list"
        name="panel"
        tag="ul"
        class="component-list"
        @before-leave="onPanelBeforeLeave"
        @after-leave="onPanelAfterLeave"
      >
        <li
          v-for="item in componentsToShow"
          :key="item.name"
          class="component-item"
        >
          <component
            :is="item.component"
            v-bind="item.props || {}"
            @hide="item.onHide"
          />
        </li>
      </TransitionGroup>
    </Transition>
  </div>
</template>

<style scoped lang="less">
@panel-duration: 360ms;
@panel-ease: cubic-bezier(0.4, 0, 0.2, 1);

.right-side-container {
  --insert-panel-duration: @panel-duration;
  --insert-panel-ease: @panel-ease;

  width: 100%;
  height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.diary-input-full {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.workspace-title {
  text-align: center;
  margin-bottom: 20px;
  flex-shrink: 0;

  h2 {
    font-size: 42px;
    font-weight: 700;
    color: #2c2c2c;
    margin: 0;
    letter-spacing: 12px;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  .empty-text {
    font-size: 18px;
    color: #999;
    text-align: center;
  }
}

// 空状态 ↔ 列表 切换
.empty-fade-enter-active,
.empty-fade-leave-active {
  transition: opacity @panel-duration @panel-ease;
}

.empty-fade-enter-from,
.empty-fade-leave-to {
  opacity: 0;
}

// 瀑布流 / Masonry 布局：纵向填满一列后自动折到下一列
.component-list {
  flex: 1;
  list-style: none;
  margin: 0;
  padding: 0;
  columns: 3;
  column-gap: 20px;
  column-fill: auto;
  position: relative;
  min-height: 0;
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.25);
    }
  }
}

.component-item {
  break-inside: avoid;
  overflow: hidden;
  margin-bottom: 20px;
  box-sizing: border-box;

  > * {
    width: 100%;
    height: auto;
  }
}

@media (max-width: 900px) {
  .component-list {
    columns: 2;
  }
}

@media (max-width: 600px) {
  .component-list {
    columns: 1;
  }
}

.panel-enter-active {
  transition:
    opacity var(--insert-panel-duration) var(--insert-panel-ease),
    transform var(--insert-panel-duration) var(--insert-panel-ease);
}

.panel-leave-active {
  transition:
    opacity var(--insert-panel-duration) var(--insert-panel-ease),
    transform var(--insert-panel-duration) var(--insert-panel-ease);
  position: absolute;
  z-index: 0;
  pointer-events: none;
}

.panel-move {
  transition: transform var(--insert-panel-duration) var(--insert-panel-ease);
  z-index: 1;
}

.panel-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.97);
}

.panel-leave-to {
  opacity: 0;
  transform: translateY(-12px) scale(0.97);
}

@media (prefers-reduced-motion: reduce) {
  .panel-enter-active,
  .panel-leave-active,
  .panel-move,
  .empty-fade-enter-active,
  .empty-fade-leave-active {
    transition-duration: 0.01ms !important;
  }

  .panel-enter-from,
  .panel-leave-to {
    transform: none;
  }
}
</style>
