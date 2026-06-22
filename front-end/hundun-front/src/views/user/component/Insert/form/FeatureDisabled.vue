<script setup>
import { useRouter } from 'vue-router'

defineProps({
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

const router = useRouter()

const goToInfo = () => {
  router.push('/user/info')
}
</script>

<template>
  <div class="feature-disabled">
    <div class="icon-wrapper">
      <span class="emoji">{{ icon }}</span>
    </div>
    <h3 class="title">{{ title }}</h3>
    <p class="message">{{ message }}</p>
    <button class="goto-btn" @click="goToInfo">前往设置</button>
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
  animation: none;
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
    animation: none;
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
  margin: 0 0 14px;
  font-size: 12px;
  color: #666;
  line-height: 1.6;
}

.goto-btn {
  padding: 7px 20px;
  background: #C3272B;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #D64A4F;
  }
}
</style>
