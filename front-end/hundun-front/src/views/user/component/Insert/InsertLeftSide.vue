<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 接收父组件传递的选中操作列表
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

// 定义事件
const emit = defineEmits(['action-select', 'update:diaryMode'])

const handleDiaryModeChange = (val) => {
  emit('update:diaryMode', val)
}

// 根据当前时间获取问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 11) {
    return '早上好'
  } else if (hour >= 11 && hour < 14) {
    return '中午好'
  } else if (hour >= 14 && hour < 18) {
    return '下午好'
  } else {
    return '晚上好'
  }
})

const username = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.username
})

// 快捷记录选项
const quickActions = [
  { label: '吃了什么', icon: '🍜' },
  { label: '去了哪里', icon: '📍' },
  { label: '买了点啥', icon: '🛒' },
  { label: '睡得咋样', icon: '😴' },
  { label: '心情如何', icon: '💭' },

  { label: '整点计划', icon: '📝' },
  { label: '重要备忘', icon: '📌' }
]

const handleQuickAction = (action) => {
  console.log('Quick action:', action.label)

  // 创建新的选中数组
  const newSelectedActions = [...props.selectedActions]

  // 切换选中状态（多选）
  const index = newSelectedActions.findIndex(a => a === action.label)
  if (index > -1) {
    // 已选中，取消选中
    newSelectedActions.splice(index, 1)
  } else {
    // 未选中，添加选中
    newSelectedActions.push(action.label)
  }

  // 触发事件通知父组件
  emit('action-select', newSelectedActions)
}
</script>

<template>
  <div class="left-side-container">
    <!-- 问候区域 -->
    <div class="greeting-section">
      <h1 class="greeting-title">
        <span class="greeting-text">{{ greeting }}</span>
        <span class="username">{{ username }} sama</span>
      </h1>
      <p class="subtitle">今天想记写什么(・ω&lt; )</p>
    </div>

    <!-- 快捷操作按钮 -->
    <div v-if="!props.diaryMode" class="quick-actions">
      <button
        v-for="action in quickActions"
        :key="action.label"
        class="action-button"
        :class="{ 'is-selected': selectedActions.includes(action.label) }"
        @click="handleQuickAction(action)"
      >
        <span class="action-icon">{{ action.icon }}</span>
        <span class="action-label">{{ action.label }}</span>
      </button>
    </div>

    <!-- 日记 AI 模式提示 -->
    <div v-else class="diary-tips">
      <p class="tip-title">AI 日记 SOLO</p>
      <p class="tip-desc">告别表单 · 写个日记 · 自动录入</p>
      <div class="tip-list-label">目前支持识别的：</div>
      <ul class="tip-list">
        <li>🍜 饮食记录</li>
        <li>💰 餐饮花费</li>
        <li>😴 睡眠记录</li>
        <li>💭 心情记录</li>
        <li>📍 外出活动</li>
        <li>🚇 交通花费</li>
        <li>🛒 消费记录</li>
        <li>📝 待办事项</li>
      </ul>
      <div class="tip-example">
        <p class="example-label">试试这样写：</p>
        <p class="example-text">"今天早上吃了两个包子和一碗粥，花了12块钱。下午去公园跑了3公里，心情不错。晚上要记得买牙膏。"</p>
      </div>
      <div class="tip-hint">
        <span class="hint-icon">💡</span>
        <span class="hint-text">尝试加明确的时间，这样识别更准确</span>
      </div>
    </div>

    <!-- 传统表单模式切换 -->
    <div class="mode-toggle" @click="handleDiaryModeChange(!props.diaryMode)">
      <div class="toggle-slider" :class="{ 'slide-right': props.diaryMode }" />
      <span class="toggle-label" :class="{ active: !props.diaryMode }">传统表单模式</span>
      <span class="toggle-label" :class="{ active: props.diaryMode }">日记 AI 模式</span>
    </div>
  </div>
</template>

<style scoped lang="less">
.left-side-container {
  width: 100%;
  height: 100%;
  padding: 32px 0 0;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.greeting-section {
  padding: 0 24px;
  animation: fadeInUp 0.6s ease-out;
}

.greeting-title {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  color: #333333;
}

.greeting-text {
  display: block;
  margin-bottom: 4px;
  color: #5A4A42;
}

.username {
  display: inline-block;
  background: linear-gradient(135deg, #C3272B 0%, #D64A4F 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.subtitle {
  margin: 0;
  font-size: 15px;
  color: #AAA;
  font-weight: 400;
  letter-spacing: 0.5px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 0 24px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: #FFF8E8;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(195, 39, 43, 0.08);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(195, 39, 43, 0.05) 0%, rgba(214, 74, 79, 0.05) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    border-color: #C3272B;
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(195, 39, 43, 0.15);

    &::before {
      opacity: 1;
    }

    .action-icon {
      transform: scale(1.15) rotate(-5deg);
    }
  }

  &:active {
    transform: translateY(0);
  }

  // 选中态样式
  &.is-selected {
    background: #C3272B;
    border-color: #C3272B;
    box-shadow: 0 4px 16px rgba(195, 39, 43, 0.3);

    .action-label {
      color: #FFFFFF;
    }

    .action-icon {
      transform: scale(1.1);
    }
  }
}

.action-icon {
  font-size: 20px;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  flex-shrink: 0;
}

.action-label {
  font-size: 14px;
  color: #454545;
  font-weight: 500;
  white-space: nowrap;
}

.diary-tips {
  padding: 0 24px;
  animation: fadeInUp 0.4s ease-out;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.tip-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #C3272B;
  text-align: center;
  letter-spacing: 2px;
}

.tip-desc {
  margin: -4px 0 0;
  font-size: 13px;
  color: #AAA;
  text-align: center;
  letter-spacing: 1px;
}

.tip-hint {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  background: rgba(195, 39, 43, 0.06);
  border-radius: 8px;
  padding: 10px 12px;
}

.hint-icon {
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 1px;
}

.hint-text {
  font-size: 12px;
  line-height: 1.5;
  color: #888;
}

.tip-list-label {
  font-size: 13px;
  font-weight: 700;
  color: #5A4A42;
  margin-top: 4px;
}

.tip-example {
  background: rgba(195, 39, 43, 0.06);
  border-radius: 8px;
  padding: 10px 12px;
  flex-shrink: 0;
}

.example-label {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 600;
  color: #C3272B;
}

.example-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: #888;
  font-style: italic;
}

.tip-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;

  li {
    font-size: 13px;
    color: #555;
    padding: 6px 10px;
    background: rgba(255, 248, 232, 0.6);
    border-radius: 6px;
    line-height: 1.4;
  }
}

.mode-toggle {
  margin-top: auto;
  width: 100%;
  padding: 0;
  border: none;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: transparent;
  cursor: pointer;
  display: flex;
  position: relative;
  flex-shrink: 0;
  height: 48px;
  overflow: hidden;
}

.toggle-slider {
  position: absolute;
  top: 6px;
  left: 6px;
  width: calc(50% - 12px);
  height: calc(100% - 12px);
  background: #C3272B;
  border-radius: 6px;
  transition: left 0.25s cubic-bezier(0.4, 0, 0.2, 1), background 0.2s ease;

  &.slide-right {
    left: calc(50% + 6px);
  }
}

.mode-toggle:hover .toggle-slider {
  background: #D64A4F;
}

.toggle-label {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #5A4A42;
  z-index: 1;
  transition: color 0.25s ease;
  user-select: none;

  &.active {
    color: #FFF;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
