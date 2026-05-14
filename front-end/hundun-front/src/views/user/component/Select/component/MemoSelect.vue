<script setup>
// TODO: 添加备忘录相关的逻辑
</script>

<template>
  <div class="memo-select">
    <div class="card-header">
      <h3 class="card-title">备忘录 📝</h3>
    </div>
    <div class="card-content">
      <p class="placeholder-text">备忘录功能开发中... (｡•̀ᴗ-)✧</p>
      <!-- TODO: 添加备忘录列表或表单 -->
    </div>
  </div>
</template>

<style scoped lang="less">
.memo-select {
  width: 100%;
  background: #FFFFFF;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
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
  min-height: 200px;
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
