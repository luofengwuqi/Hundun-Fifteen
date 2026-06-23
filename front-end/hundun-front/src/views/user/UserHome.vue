<script setup>

import HundunHeader from '@/views/user/component/common/HundunHeader.vue'
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user.js'
import GuideOverlay from '@/views/user/component/guide/GuideOverlay.vue'

const userStore = useUserStore()
const showGuide = ref(false)

onMounted( async () => {
  if(!userStore.userModelStatus || Object.keys(userStore.userModelStatus).length === 0){
    await userStore.initUserModelStatus()
    console.log(userStore.userModelStatus)
  }
  if (userStore.userModelStatus.isNewUser === 1) {
    showGuide.value = true
  }
})

const handleGuideClose = () => {
  showGuide.value = false
}
</script>

<template>
  <div class="user-home">
    <div class="header">
      <HundunHeader></HundunHeader>
    </div>
    <div class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="$route.fullPath" />
        </transition>
      </router-view>
    </div>
    <GuideOverlay v-if="showGuide" @close="handleGuideClose" />
  </div>
</template>



<style scoped lang="less">
  .user-home{
    width: 100%;
    height: 100%;
  }
  .header{
    height: 80px;
  }
  .main{
    height: calc(100vh - 80px);
    overflow: auto;

    &::-webkit-scrollbar {
      display: none;
    }

    -ms-overflow-style: none;
    scrollbar-width: none;
  }

  /* 二级路由过渡动画 */
  :deep(.fade-enter-from) {
    opacity: 0;
    transform: translateY(20px);
  }
  :deep(.fade-enter-active) {
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  }
  :deep(.fade-leave-to) {
    opacity: 0;
    transform: translateY(-20px);
  }
  :deep(.fade-leave-active) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
</style>
