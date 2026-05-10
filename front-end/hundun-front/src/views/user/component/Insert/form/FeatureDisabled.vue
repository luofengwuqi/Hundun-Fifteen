<script setup>
// 定义组件属性
const props = defineProps({
  title: {
    type: String,
    default: '功能未启用 (灬º﹃º灬)'
  },
  message: {
    type: String,
    default: '使用当前组件请先启用相关模块'
  },
  icon: {
    type: String,
    default: '🔒'
  }
})
</script>

<template>
  <div class="feature-disabled">
    <div class="icon-wrapper">
      <span class="emoji">{{ icon }}</span>
    </div>
    <h3 class="title">{{ title }}</h3>
    <p class="message">{{ message }}</p>
  </div>
</template>

<style scoped lang="less">
.feature-disabled {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #FFF8E8 0%, #FFE8E8 100%);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 2px dashed #C3272B;
  animation: fadeIn 0.5s ease-out;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  margin-bottom: 12px;
  
  .emoji {
    font-size: 36px;
    display: inline-block;
    animation: bounce 2s infinite;
  }
}

.title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #C3272B;
  letter-spacing: 1px;
}

.message {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
</style>
