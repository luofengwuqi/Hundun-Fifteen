<script setup>
  import { useUserStore } from '@/stores/user.js'
  import { onMounted, ref } from 'vue'

  onMounted(() => {
  // 或者调用 userStore 的某个方法

})


  const userStore = useUserStore()
  const ban = ref(false)
  const handleClick = () => {
    ban.value = !ban.value
  }

  const showDropdown = ref(false)

  const handleMouseEnter = () => {
    showDropdown.value = true
  }

  const handleMouseLeave = () => {
    showDropdown.value = false
  }
</script>

<template>
  <div
    class="music"
    v-if="userStore.userModelStatus.music === 1"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="main-button" @click="handleClick">
      <img v-if="ban" src="@/assets/img/music/music_icon_ban.png" alt="">
      <img v-else src="@/assets/img/music/music_icon.png" alt="">
    </div>
    <div class="menu" v-if="showDropdown">
<!--   TODO:当前正在播放:... 跳转到...   -->
    </div>
  </div>
</template>

<style scoped lang="less">
  .music{
    z-index: 99;
    position: absolute;
    top: 100px ;
    right:40px ;
    .main-button{
      width: 50px;
      height: 50px;
      //透明度
      opacity: 0.3;

      padding: 10px;
      border-radius: 50%;
      overflow: hidden;
      background-color: #FFEFD5;
      //过渡渐变
      transition: all 0.5s;
      &:hover{
        box-shadow: 0 0 10px rgba(0,0,0,0.5);
        opacity: 1;
      }
    }
    .menu{
      position: absolute;
    }
  }
</style>
