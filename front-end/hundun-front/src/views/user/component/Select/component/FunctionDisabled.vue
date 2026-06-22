<script setup>
import { useRouter } from 'vue-router'

defineProps({
  moduleName: {
    type: String,
    default: '该功能'
  }
})

const router = useRouter()

const goToInfo = () => {
  router.push('/user/info')
}

const goToHome = () => {
  router.push('/user/home')
}
</script>

<template>
  <div class="function-disabled">
    <div class="fd-icon">🔒</div>
    <div class="fd-title">功能未开启</div>
    <div class="fd-desc">「{{ moduleName }}」模块未启用</div>
    <div class="fd-actions">
      <button class="fd-btn primary" @click="goToInfo">前往设置</button>
      <button class="fd-btn" @click="goToHome">返回主页</button>
    </div>
  </div>
</template>

<style scoped lang="less">
@red: #C3272B;
@red-hover: #D64A4F;
@cream: #FFF8E8;
@cream-dark: #FFEFD5;
@text: #5C3D2E;
@text-muted: #8B7355;

.function-disabled {
  width: 100%;
  height: 100%;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: @cream;
  border-radius: 12px;
}

.fd-icon {
  font-size: 48px;
  line-height: 1;
}

.fd-title {
  font-size: 20px;
  font-weight: 700;
  color: @text;
  letter-spacing: 2px;
}

.fd-desc {
  font-size: 13px;
  color: @text-muted;
  text-align: center;
  line-height: 1.7;
  margin-bottom: 4px;
}

.fd-actions {
  display: flex;
  gap: 12px;
}

.fd-btn {
  padding: 8px 20px;
  border-radius: 8px;
  border: 1px solid @cream-dark;
  background: transparent;
  color: @text;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: @cream-dark;
    color: @red;
  }

  &.primary {
    background: @red;
    color: @cream;
    border: none;

    &:hover {
      background: @red-hover;
    }
  }
}
</style>
