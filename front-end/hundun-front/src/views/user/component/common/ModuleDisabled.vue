<script setup>
import { useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const router = useRouter()

const moduleName = computed(() => {
  const map = { knowledge: '知识', music: '音乐' }
  return map[route.query.module] || '该'
})

const handleGoInfo = () => {
  router.push('/user/info')
}
</script>

<template>
  <div class="disabled-page">
    <div class="card">
      <div class="icon">⚠️</div>
      <h2>{{ moduleName }}模块未启用</h2>
      <p>请在个人信息页中开启对应模块后使用</p>
      <el-button type="primary" @click="handleGoInfo" style="background-color: #C3272B; border: none;">
        前往个人信息页
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="less">
.disabled-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.card {
  text-align: center;
  padding: 60px 80px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
  .icon {
    font-size: 64px;
    margin-bottom: 20px;
  }
  h2 {
    margin: 0 0 12px;
    color: #5C3D2E;
  }
  p {
    margin: 0 0 28px;
    color: #8B7355;
  }
}
</style>
