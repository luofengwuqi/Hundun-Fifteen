<script setup>
import { ref, watch } from 'vue'

// 定义事件
const emit = defineEmits(['category-select'])

// 接收父组件传递的初始选中分类
const props = defineProps({
  initialCategory: {
    type: String,
    default: ''
  }
})

// 分类选项列表（响应式，方便以后扩展）
const categories = ref([
  { label: '健康', icon: '💪', value: 'health' },
  { label: '财务', icon: '💰', value: 'finance' },
  { label: '知识', icon: '📚', value: 'knowledge' },
  { label: '备忘', icon: '📝', value: 'memo' }
])

// 当前选中的分类（使用 props 初始化）
const selectedCategory = ref(props.initialCategory)

// 监听 props 变化，同步更新
watch(() => props.initialCategory, (newVal) => {
  if (newVal) {
    selectedCategory.value = newVal
  }
})

const handleCategoryClick = (category) => {
  console.log('Selected category:', category.value)
  selectedCategory.value = category.value
  // 触发事件通知父组件
  emit('category-select', category.value)
}
</script>

<template>
  <div class="left-side-container">
    <!-- 标题区域 -->
    <div class="header-section">
      <h2 class="section-title">探索记录</h2>
      <p class="section-subtitle">选择你想查看的内容 (｡•̀ᴗ-)✧</p>
    </div>

    <!-- 分类选项列表 -->
    <div class="categories-list">
      <button
        v-for="category in categories"
        :key="category.value"
        class="category-button"
        :class="{ 'is-selected': selectedCategory === category.value }"
        @click="handleCategoryClick(category)"
      >
        <span class="category-icon">{{ category.icon }}</span>
        <span class="category-label">{{ category.label }}</span>
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

.header-section {
  animation: fadeInUp 0.6s ease-out;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 32px;
  font-weight: 700;
  line-height: 1.3;
  color: #333333;
  text-align: center;
}

.section-subtitle {
  margin: 0;
  font-size: 16px;
  color: #AAA;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-align: center;
}

.categories-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.category-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 20px 24px;
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
    transform: translateY(-2px) translateX(4px);
    box-shadow: 0 4px 16px rgba(195, 39, 43, 0.15);

    &::before {
      opacity: 1;
    }

    .category-icon {
      transform: scale(1.15) rotate(-5deg);
    }
  }

  &:active {
    transform: translateY(0) translateX(2px);
  }

  // 选中态样式
  &.is-selected {
    background: linear-gradient(135deg, #C3272B 0%, #D64A4F 100%);
    border-color: #C3272B;
    box-shadow: 0 4px 16px rgba(195, 39, 43, 0.3);

    .category-label {
      color: #FFFFFF;
    }

    .category-icon {
      transform: scale(1.1);
    }
  }
}

.category-icon {
  font-size: 24px;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  flex-shrink: 0;
}

.category-label {
  font-size: 18px;
  color: #454545;
  font-weight: 600;
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
