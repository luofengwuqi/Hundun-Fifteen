<script setup>
import { ref } from 'vue'
import InsertLeftSide from '@/views/user/component/Insert/InsertLeftSide.vue'
import InsertRightSide from '@/views/user/component/Insert/InsertRightSide.vue'

// 当前选中的操作列表
const selectedActions = ref([])
// 日记 AI 模式
const diaryMode = ref(false)
// 跟踪已隐藏的组件
const hiddenComponents = ref(new Set())

const handleActionSelect = (actions) => {
  console.log('Selected actions:', actions)
  selectedActions.value = actions
  // 当选中操作变化时，清空隐藏状态
  hiddenComponents.value.clear()
}

const handleHideComponent = (componentName) => {
  console.log('Hidden component:', componentName)
  hiddenComponents.value.add(componentName)
  
  // 检查是否两个组件都被隐藏了
  if (hiddenComponents.value.has('WhatAteToday') && hiddenComponents.value.has('MealCost')) {
    console.log('Both components hidden, clearing selection')
    // 直接调用左侧组件的方法来取消选中
    // 通过重新设置 selectedActions 来触发左侧更新
    const newActions = selectedActions.value.filter(action => action !== '吃了什么')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }
  
  // 检查是否是 MemoCommon 组件被隐藏
  if (componentName === 'MemoCommon') {
    console.log('MemoCommon hidden, clearing selection')
    // 从选中列表中移除"整点计划"
    const newActions = selectedActions.value.filter(action => action !== '整点计划')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }

  if (componentName === 'MemoImportant') {
    const newActions = selectedActions.value.filter(action => action !== '重要备忘')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }

  if (componentName === 'ArticleCreate') {
    const newActions = selectedActions.value.filter(action => action !== '今日芝士')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }

  if (componentName === 'SleepHealth') {
    const newActions = selectedActions.value.filter(action => action !== '睡得咋样')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }

  if (componentName === 'MoodHealth') {
    const newActions = selectedActions.value.filter(action => action !== '心情如何')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }

  if (componentName === 'PlaceWhere') {
    const newActions = selectedActions.value.filter(action => action !== '去了哪里')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }

  // 检查是否是 Payment 组件被隐藏
  if (componentName === 'Payment') {
    console.log('Payment hidden, clearing selection')
    // 从选中列表中移除"买了点啥"
    const newActions = selectedActions.value.filter(action => action !== '买了点啥')
    selectedActions.value = [...newActions]
    hiddenComponents.value.clear()
  }
}
</script>

<template>
  <div class="insert-container">
    <div class="insert-layout">
      <div class="left-panel">
        <InsertLeftSide 
          :selected-actions="selectedActions" 
          :diary-mode="diaryMode"
          @action-select="handleActionSelect"
          @update:diary-mode="diaryMode = $event"
        />
      </div>
      <div class="right-panel">
        <InsertRightSide 
          :selected-actions="selectedActions" 
          :diary-mode="diaryMode"
          @hide-component="handleHideComponent"
          @update:diary-mode="diaryMode = $event"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.insert-container {
  width: 100%;
  height: calc(100vh - 80px);
  background: transparent;
  overflow: hidden;
}

.insert-layout {
  display: flex;
  width: 100%;
  height: 100%;
  gap: 24px;
  padding: 24px;
}

.left-panel {
  flex: 0 0 25%;
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
</style>
