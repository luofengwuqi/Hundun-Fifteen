<script setup>
import InsertLeftSide from '@/views/user/component/Insert/InsertLeftSide.vue'
import InsertRightSide from '@/views/user/component/Insert/InsertRightSide.vue'
</script>

<template>
  <div class="insert-container">
    <div class="insert-layout">
      <div class="left-panel">
        <InsertLeftSide />
      </div>
      <div class="right-panel">
        <InsertRightSide />
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
