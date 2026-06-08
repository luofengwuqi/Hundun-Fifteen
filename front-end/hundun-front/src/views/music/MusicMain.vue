<script setup>
  import { ref } from 'vue'

  const ban = ref(false)
  const handleClick = () => {
    ban.value = !ban.value
  }
</script>

<template>
  <div class="music" @click="handleClick">
    <img v-if="ban" src="@/assets/img/music/music_icon_ban.png" alt="">
    <img v-else src="@/assets/img/music/music_icon.png" alt="">
  </div>
</template>

<style scoped lang="less">
  .music{
    z-index: 99;
    position: absolute;
    top: 100px ;
    right:40px ;
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
</style>
