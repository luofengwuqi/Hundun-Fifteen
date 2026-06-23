<script setup>
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()
</script>

<template>
  <div class="page">
    <div class="emoji">🫧</div>
    <h1 class="title">欢迎 {{ userStore.userInfo?.nickname || userStore.userInfo?.username }}！</h1>
    <p class="subtitle">欢迎来到一五馄饨 <span class="wave">(∠·ω&lt; )⌒★</span></p>
    <div class="divider"></div>
    <p class="desc">
      在开始前，请允许我们啰嗦几句——<br>
      这里是一个自由随性的记录空间，<br>
      你可以选择开启或关闭某些功能模块，<br>
      让这里只属于你。
    </p>
  </div>
</template>

<style scoped lang="less">
@text: #5C3D2E;
@text-muted: #8B7355;
@red: #C3272B;

.page {
  text-align: center;
  padding: 20px 0;
}

.emoji {
  font-size: 56px;
  margin-bottom: 12px;
  line-height: 1;
}

.title {
  font-size: 26px;
  font-weight: 600;
  color: @text;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 16px;
  color: @text-muted;
  margin: 0 0 6px;

  .wave {
    color: @red;
    display: inline-block;
  }
}

.divider {
  width: 48px;
  height: 3px;
  background: @red;
  border-radius: 2px;
  margin: 16px auto;
  opacity: 0.4;
}

.desc {
  font-size: 14px;
  color: @text-muted;
  line-height: 1.8;
  margin: 0;
  max-width: 380px;
}
</style>
