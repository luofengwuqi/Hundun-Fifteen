<script setup>
import { RouterView, useRoute } from 'vue-router'
import MusicMain from '@/views/music/MusicMain.vue'

const route = useRoute()
</script>

<template>
  <div class="app">
    <MusicMain></MusicMain>
    <!-- 路由过渡动画：使用作用域插槽 -->
    <!-- 用 $route.fullPath 作为 key，确保 query/params 变化时组件完全重建 -->
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" :key="$route.fullPath" />
      </transition>
    </router-view>
  </div>
</template>

<style scoped>
.app {
  width: 100vw;
  height: 100vh;
  background: url("@/assets/img/common/background.jpg") no-repeat center fixed;
  background-size: cover;
  /* 让页面占满，隐藏滚动条（可选优化） */
  overflow: hidden;
}

/* 路由平滑过渡动画 */
:deep(.fade-enter-from) {
  opacity: 0;
  transform: translateX(10px);
}
:deep(.fade-enter-active) {
  transition: all 0.6s ease-out;
}
:deep(.fade-enter-to) {
  opacity: 1;
  transform: translateX(0);
}
:deep(.fade-leave-from) {
  opacity: 1;
}
:deep(.fade-leave-active) {
  transition: all 0.5s ease-in;
}
:deep(.fade-leave-to) {
  opacity: 0;
}
</style>
