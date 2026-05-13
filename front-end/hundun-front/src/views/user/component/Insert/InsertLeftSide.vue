<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 接收父组件传递的选中操作列表
const props = defineProps({
  selectedActions: {
    type: Array,
    default: () => []
  }
})

// 定义事件
const emit = defineEmits(['action-select'])

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
  { label: '睡得咋样', icon: '😴' },
  { label: '心情如何', icon: '💭' },
  { label: '遇到趣事', icon: '✨' },
  { label: '今日感悟', icon: '🌟' },
  { label: '备忘记录', icon: '📝' }
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
    <div class="quick-actions">
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
  </div>
</template>

<style scoped lang="less">
.left-side-container {
  width: 100%;
  height: 100%;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.greeting-section {
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
