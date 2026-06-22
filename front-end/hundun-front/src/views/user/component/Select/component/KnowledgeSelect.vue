<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user.js'
import FunctionDisabled from './FunctionDisabled.vue'

const userStore = useUserStore()
const enabled = computed(() => userStore.userModelStatus?.knowledge === 1)
</script>

<template>
  <FunctionDisabled v-if="!enabled" module-name="知识" />
  <div v-else class="knowledge-select select-panel">
    <div class="card-header">
      <h3 class="card-title">知识收藏 📚</h3>
    </div>
    <div class="card-content">
      <p class="placeholder-text">知识收藏功能开发中... (｡•̀ᴗ-)✧</p>
    </div>
  </div>
</template>

<style scoped lang="less">
.knowledge-select {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 0;
  padding: 22px 28px;
  box-shadow: none;
  box-sizing: border-box;
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #FFF8E8;
}

.card-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #333333;
}

.card-content {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 16px;
  color: #999;
  text-align: center;
}
</style>
