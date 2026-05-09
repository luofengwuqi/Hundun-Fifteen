<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import WhatAteToday from './form/WhatAteToday.vue'
import MealCost from './form/MealCost.vue'
import FeatureDisabled from './form/FeatureDisabled.vue'

const userStore = useUserStore()

// 接收父组件传递的选中操作列表
const props = defineProps({
  selectedActions: {
    type: Array,
    default: () => []
  }
})

// 定义事件
const emit = defineEmits(['hide-component'])

// 检查是否启用了财务和健康模块
const isModulesEnabled = computed(() => {
  const status = userStore.userModelStatus
  return status && status.health === 1 && status.finance === 1
})

// 跟踪隐藏的组件
const hiddenComponents = ref(new Set())

const handleHideComponent = (componentName) => {
  hiddenComponents.value.add(componentName)
  emit('hide-component', componentName)
}

// 根据选中的操作和模块状态，决定显示哪些组件
const componentsToShow = computed(() => {
  const components = []
  
  // 检查是否选中了"吃了什么"
  const hasSelectedEat = props.selectedActions.includes('吃了什么')
  
  if (hasSelectedEat) {
    // 如果选中了“吃了什么”，检查模块是否启用
    if (!isModulesEnabled.value) {
      // 模块未启用，显示功能禁用组件
      components.push({ 
        name: 'FeatureDisabled', 
        component: FeatureDisabled,
        props: {
          title: '功能未启用 (灬º﹃º灬)',
          message: '使用当前组件请先启用财务模块和健康模块',
          icon: '🔒'
        }
      })
    } else {
      // 模块已启用，显示正常组件（如果未被隐藏）
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
  
  // TODO: 其他操作的逻辑
  if (props.selectedActions.includes('去了哪里')) {
    // TODO: 添加对应的组件
    console.log('TODO: 添加去了哪里的组件')
  }
  if (props.selectedActions.includes('睡得咋样')) {
    // TODO: 添加对应的组件
    console.log('TODO: 添加睡得咋样的组件')
  }
  if (props.selectedActions.includes('心情如何')) {
    // TODO: 添加对应的组件
    console.log('TODO: 添加心情如何的组件')
  }
  if (props.selectedActions.includes('遇到趣事')) {
    // TODO: 添加对应的组件
    console.log('TODO: 添加遇到趣事的组件')
  }
  if (props.selectedActions.includes('今日感悟')) {
    // TODO: 添加对应的组件
    console.log('TODO: 添加今日感悟的组件')
  }
  
  return components
})

// 监听选中操作变化，重置隐藏状态
import { watch } from 'vue'
watch(() => props.selectedActions, () => {
  // 当选中操作变化时，清空隐藏状态
  hiddenComponents.value.clear()
}, { deep: true })
</script>

<template>
  <div class="right-side-container">
    <!-- 工作台标题 -->
    <div class="workspace-title">
      <h2>工作台</h2>
    </div>
    
    <!-- 空状态提示 -->
    <div v-if="componentsToShow.length === 0" class="empty-state">
      <p class="empty-text">请选择左侧的操作项 (・ω< )</p>
    </div>
    <!-- 组件列表 -->
    <ul v-else class="component-list">
      <li v-for="item in componentsToShow" :key="item.name" class="component-item">
        <component 
          :is="item.component" 
          v-bind="item.props || {}" 
          @hide="item.onHide"
        />
      </li>
    </ul>
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

.workspace-title {
  text-align: center;
  margin-bottom: 32px;
  
  h2 {
    font-size: 42px;
    font-weight: 700;
    color: #2C2C2C;
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

// 类似 ul > li 的流式布局
.component-list {
  flex: 1;
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  overflow-y: auto;
  align-content: flex-start;
  
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
  // 正方形组件，宽度自适应
  flex: 0 0 calc(33.333% - 14px);
  aspect-ratio: 1 / 1;
  min-width: 250px;
  
  // 让内部组件填满整个 li
  > * {
    width: 100%;
    height: 100%;
  }
  
  // 组件进入动画
  animation: componentEnter 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

@keyframes componentEnter {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
</style>
