<script setup>
import { computed } from 'vue'
import HealthSelect from './component/HealthSelect.vue'
import FinanceSelect from './component/FinanceSelect.vue'
import KnowledgeSelect from './component/KnowledgeSelect.vue'
import MemoSelect from './component/MemoSelect.vue'

// 接收父组件传递的选中分类
const props = defineProps({
  selectedCategory: {
    type: String,
    default: ''
  }
})

// 根据选中的分类，决定显示哪个组件
const currentComponent = computed(() => {
  const componentMap = {
    health: HealthSelect,
    finance: FinanceSelect,
    knowledge: KnowledgeSelect,
    memo: MemoSelect
  }
  
  return componentMap[props.selectedCategory] || null
})

// 获取当前组件的标题
const currentTitle = computed(() => {
  const titleMap = {
    health: '健康记录',
    finance: '财务记录',
    knowledge: '知识收藏',
    memo: '备忘录'
  }
  
  return titleMap[props.selectedCategory] || ''
})
</script>

<template>
  <div class="right-side-container">
    <!-- 空状态提示 -->
    <div v-if="!selectedCategory" class="empty-state">
      <p class="empty-text">请选择左侧的分类查看内容 (・ω< )</p>
    </div>
    
    <!-- 动态组件展示区域 -->
    <div v-else class="content-area">
      <Transition name="fade-slide" mode="out-in">
        <component :is="currentComponent" :key="selectedCategory" />
      </Transition>
    </div>
  </div>
</template>

<style scoped lang="less">
.right-side-container {
  width: 100%;
  height: 100%;
  padding: 24px;
  display: flex;
  flex-direction: column;
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

.content-area {
  flex: 1;
  overflow-y: auto;
  
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

// 组件切换过渡动画
.fade-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>
