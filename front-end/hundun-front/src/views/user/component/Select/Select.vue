<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import SelectLeftSide from './SelectLeftSide.vue'
import SelectRightSide from './SelectRightSide.vue'

const route = useRoute()

// 当前选中的分类
const selectedCategory = ref('')

// 组件挂载时检查是否有 query 参数
onMounted(() => {
  const category = route.query.category
  if (category) {
    selectedCategory.value = category
  }
})

// 监听路由变化，支持直接访问带参数的 URL
watch(() => route.query.category, (newCategory) => {
  if (newCategory) {
    selectedCategory.value = newCategory
  }
})

const handleCategorySelect = (category) => {
  console.log('Selected category:', category)
  selectedCategory.value = category
}
</script>

<template>
  <div class="select-container">
    <div class="select-layout">
      <div class="left-panel">
        <SelectLeftSide 
          :initial-category="selectedCategory"
          @category-select="handleCategorySelect" 
        />
      </div>
      <div class="right-panel">
        <SelectRightSide :selected-category="selectedCategory" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.select-container {
  width: 100%;
  height: calc(100vh - 80px);
  background: transparent;
  overflow: hidden;
}

.select-layout {
  display: flex;
  width: 100%;
  height: 100%;
  gap: 24px;
  padding: 24px;
}

.left-panel {
  flex: 0 0 22%;
  height: 100%;
  background: rgba(255, 248, 232, 0.65);
  border-radius: 16px;
  overflow: hidden;
  overflow-y: auto;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  
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

.right-panel {
  flex: 1;
  height: 100%;
  min-width: 0;
  background: rgba(255, 248, 232, 0.65);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  
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
</style>
