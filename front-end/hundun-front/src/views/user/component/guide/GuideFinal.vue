<script setup>
</script>

<template>
  <div class="page">
    <div class="emoji">✨</div>
    <h1 class="title">准备好了！</h1>
    <p class="subtitle">开始记录之旅吧 (∠・ω< )⌒★</p>
    <div class="divider"></div>
    <p class="desc">
      你已经完成了基础设置，<br>
      随时可以在个人信息页调整模块开关。
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
  color: @red;
  margin: 0 0 6px;
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
