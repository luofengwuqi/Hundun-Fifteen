<script setup>
// 首页组件 - 录入与翻找功能入口
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import insertImage from '@/assets/img/user/home/insert.png'
import selectImage from '@/assets/img/user/home/select.png'
import healthImage from '@/assets/img/user/home/health.png'
import financeImage from '@/assets/img/user/home/finance.png'
import knowledgeImage from '@/assets/img/user/home/knowledge.png'
import memoImage from '@/assets/img/user/home/memo.png'
import { useUserStore } from '@/stores/user.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const isExpanded = ref(false)
const isAnimating = ref(false) // 控制动画状态

//pinia
const userStore = useUserStore()
onMounted( async () => {
  //组件被渲染时 初始化用户状态
  await userStore.initUserModelStatus()
  console.log(userStore.userModelStatus)
})

// 长按充能相关状态
const chargingBoxId = ref(null) // 当前正在充能的盒子ID
const chargeProgress = ref(0) // 充能进度 0-100
const isChargingFailed = ref(false) // 充能失败状态(用于动画)
let chargeTimer = null
let chargeStartTime = null
let fadeOutTimer = null // 淡出定时器
const CHARGE_DURATION = 800 // 充能持续时间(毫秒)
const FADE_OUT_DURATION = 400 // 淡出动画时长

const handleBoxClick = (boxType) => {
  if (boxType === 'left') {
    // 左侧盒子：跳转到录入页面
    router.push('/user/insert')
  } else if (boxType === 'right' && !isExpanded.value && !isAnimating.value) {
    isAnimating.value = true
    // 立即触发展开动画，左侧盒子开始飘出
    isExpanded.value = true
    // 动画结束后重置状态
    setTimeout(() => {
      isAnimating.value = false
    }, 800)
  }
}

const closeExpanded = () => {
  if (!isAnimating.value) {
    isAnimating.value = true
    isExpanded.value = false
    setTimeout(() => {
      isAnimating.value = false
    }, 600)
  }
}

// 四个分类的数据（原始数据）
const categoriesBase = [
  {
    id: 1,
    title: '健康',
    originalSubtitle: '人是铁 饭是钢 一顿不吃饿得慌',
    image: healthImage,
    path: '/user/select/health',
    statusKey: 'health'
  },
  {
    id: 2,
    title: '财务',
    originalSubtitle: '金钱不是万能的 但没有钱是万万不能的',
    image: financeImage,
    path: '/user/select/finance',
    statusKey: 'finance'
  },
  {
    id: 3,
    title: '知识',
    originalSubtitle: '芝士 就是力量',
    image: knowledgeImage,
    path: '/user/select/knowledge',
    statusKey: 'knowledge'
  },
  {
    id: 4,
    title: '备忘',
    originalSubtitle: '活着不是为了不朽 而是为了创造不朽',
    image: memoImage,
    path: '/user/select/memo',
    statusKey: 'memo'
  }
]

// 根据用户状态动态生成分类数据
const categories = computed(() => {
  const status = userStore.userModelStatus || {}
  return categoriesBase.map(category => {
    const isEnabled = status[category.statusKey] === 1
    return {
      ...category,
      subtitle: isEnabled ? category.originalSubtitle : '未启用',
      enabled: isEnabled
    }
  })
})

// 处理小盒子点击事件
const handleCategoryClick = (category) => {
  // 只有启用的盒子才能点击跳转
  if (category.enabled && category.path) {
    router.push(category.path)
  }
}

// 长按开始
const handleLongPressStart = (category) => {
  // 只对未启用的盒子生效
  if (category.enabled) return
  
  chargingBoxId.value = category.id
  chargeProgress.value = 0
  chargeStartTime = Date.now()
  
  // 启动充能动画
  chargeTimer = setInterval(() => {
    const elapsed = Date.now() - chargeStartTime
    const progress = Math.min((elapsed / CHARGE_DURATION) * 100, 100)
    chargeProgress.value = progress
    
    // 充能完成
    if (progress >= 100) {
      completeCharging(category)
    }
  }, 16) // 约60fps
}

// 长按结束
const handleLongPressEnd = () => {
  if (chargeTimer) {
    clearInterval(chargeTimer)
    chargeTimer = null
  }
  
  // 如果未完成充能,触发能量消散动画
  if (chargeProgress.value < 100 && chargeProgress.value > 0) {
    isChargingFailed.value = true
    
    // 清除之前的淡出定时器
    if (fadeOutTimer) {
      clearTimeout(fadeOutTimer)
    }
    
    // 等待消散动画完成后重置状态
    fadeOutTimer = setTimeout(() => {
      chargeProgress.value = 0
      chargingBoxId.value = null
      isChargingFailed.value = false
      fadeOutTimer = null
    }, FADE_OUT_DURATION)
  } else if (chargeProgress.value === 0) {
    // 如果刚开始就松开,立即重置
    chargingBoxId.value = null
  }
}

// 充能完成
const completeCharging = async (category) => {
  // 清除定时器
  if (chargeTimer) {
    clearInterval(chargeTimer)
    chargeTimer = null
  }
  
  try {
    // TODO: 调用启用接口
    // await enableCategoryAPI(category.statusKey)
    
    // 更新本地状态
    await userStore.enableCategory(category.statusKey)
    
    ElMessage.success(`${category.title}模块已启用!`)
    
    // 延迟重置充能状态
    setTimeout(() => {
      chargeProgress.value = 0
      chargingBoxId.value = null
      isChargingFailed.value = false
    }, 500)
  } catch (error) {
    console.error('启用失败:', error)
    ElMessage.error('启用失败,请重试')
    isChargingFailed.value = true
    
    // 失败时也触发动画
    setTimeout(() => {
      chargeProgress.value = 0
      chargingBoxId.value = null
      isChargingFailed.value = false
    }, FADE_OUT_DURATION)
  }
}
</script>

<template>
  <div class="home-container">
    <!-- 返回按钮（展开时显示） -->
    <transition name="fade">
      <div v-if="isExpanded" class="back-button" @click="closeExpanded">
        <span>← 返回</span>
      </div>
    </transition>

    <div class="boxes-wrapper">
      <!-- 左侧盒子：录入 -->
      <div
        v-if="!isExpanded"
        class="box box-left"
        @click="handleBoxClick('left')"
      >
        <div class="box-content">
          <div class="image-wrapper">
            <img :src="insertImage" alt="录入" class="box-image" />
          </div>
          <h2 class="box-title">录入</h2>
          <p class="box-subtitle">记录当前的坐标</p>
        </div>
      </div>

      <!-- 右侧盒子：翻找（未展开状态） -->
      <div
        v-if="!isExpanded"
        class="box box-right"
        @click="handleBoxClick('right')"
      >
        <div class="box-content">
          <div class="image-wrapper">
            <img :src="selectImage" alt="翻找" class="box-image" />
          </div>
          <h2 class="box-title">翻找</h2>
          <p class="box-subtitle">看看来时的路</p>
        </div>
      </div>

      <!-- 展开后的四个小盒子 -->
      <div v-if="isExpanded" class="split-boxes-wrapper">
        <div
          v-for="(category, index) in categories"
          :key="category.id"
          class="split-box"
          :class="{ 
            'disabled': !category.enabled,
            'charging': chargingBoxId === category.id && !category.enabled,
            'charging-failed': isChargingFailed && chargingBoxId === category.id
          }"
          :style="{ animationDelay: `${index * 0.08}s` }"
          @click="handleCategoryClick(category)"
          @mousedown="!category.enabled && handleLongPressStart(category)"
          @mouseup="handleLongPressEnd"
          @mouseleave="handleLongPressEnd"
          @touchstart="!category.enabled && handleLongPressStart(category)"
          @touchend="handleLongPressEnd"
        >
          <!-- 充能进度条 -->
          <div 
            v-if="chargingBoxId === category.id && !category.enabled"
            class="charge-progress-bar"
            :class="{ 'failing': isChargingFailed }"
            :style="{ height: `${chargeProgress}%` }"
          >
            <div class="charge-glow"></div>
          </div>
          
          <div class="split-box-content">
            <div class="split-image-wrapper">
              <img :src="category.image" :alt="category.title" class="split-box-image" />
            </div>
            <h3 class="split-box-title">{{ category.title }}</h3>
            <p class="split-box-subtitle">{{ category.subtitle }}</p>
            
            <!-- 充能提示文字 -->
            <p v-if="chargingBoxId === category.id && !category.enabled" class="charge-hint">
              启用中
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.home-container {
  min-height: calc(100vh - 80px);
  width: 100%;
  padding: 80px 60px;
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
}

.boxes-wrapper {
  position: relative;
  width: 100%;
  max-width: 1400px;
  min-height: 480px;
}

.box {
  position: absolute;
  top: 0;
  width: calc(50% - 25px);
  min-height: 480px;
  background-color: #FFF8E8;
  border-radius: 20px;
  padding: 50px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #C3272B 0%, #D64A4F 100%);
    opacity: 0;
    transition: opacity 0.6s ease;
  }

  &:hover {
    transform: scale(1.12);
    box-shadow: 0 25px 60px rgba(195, 39, 43, 0.25),
                0 12px 30px rgba(0, 0, 0, 0.15);
    z-index: 10;

    &::before {
      opacity: 1;
    }

    .box-content {
      transform: scale(1.05);
    }

    .image-placeholder {
      border-color: #C3272B;
      background-color: #FFF8E8;
      box-shadow: 0 12px 30px rgba(195, 39, 43, 0.2);

      .placeholder-text {
        color: #C3272B;
      }
    }

    .box-title {
      color: #C3272B;
      transform: translateY(-5px);
    }

    .box-subtitle {
      color: #5A4A42;
      opacity: 0.9;
    }
  }
}

.box-left {
  left: 0;
}

.box-right {
  right: 0;
}

.box-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 35px;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  width: 100%;
}

.image-wrapper {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.box-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 16px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  border: 2px dashed #AAA;
  border-radius: 16px;
  background-color: #FFFBF8;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &::after {
    content: 'TODO: 替换为实际图片';
    position: absolute;
    bottom: -28px;
    font-size: 12px;
    color: #AAA;
    white-space: nowrap;
  }
}

.placeholder-text {
  font-size: 14px;
  color: #AAA;
  transition: color 0.6s ease;
}

.box-title {
  font-size: 40px;
  font-weight: 700;
  color: #2C2C2C;
  margin: 0;
  letter-spacing: 10px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

.box-subtitle {
  font-size: 16px;
  font-weight: 400;
  color: #666;
  margin: 0;
  letter-spacing: 2px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

// 返回按钮
.back-button {
  position: fixed;
  top: 100px; // 从30px调整到60px，往下移动
  left: 30px;
  padding: 12px 24px;
  background-color: #C3272B;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(195, 39, 43, 0.3);
  transition: all 0.3s ease;

  &:hover {
    background-color: #D64A4F;
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(195, 39, 43, 0.4);
  }
}

// 分割后的四个小盒子容器
.split-boxes-wrapper {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  width: 100%;
  max-width: 1000px;
  padding: 20px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.split-box {
  min-height: 320px;
  background-color: #FFF8E8;
  border-radius: 16px;
  padding: 35px 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #C3272B 0%, #D64A4F 100%);
    opacity: 0;
    transition: opacity 0.4s ease;
  }

  &:hover {
    transform: translateY(-8px) scale(1.03);
    box-shadow: 0 16px 40px rgba(195, 39, 43, 0.2),
                0 8px 20px rgba(0, 0, 0, 0.12);

    &::before {
      opacity: 1;
    }

    .split-image-placeholder {
      border-color: #C3272B;
      background-color: #FFF8E8;
      box-shadow: 0 8px 20px rgba(195, 39, 43, 0.15);

      .placeholder-text {
        color: #C3272B;
      }
    }

    .split-box-title {
      color: #C3272B;
      transform: translateY(-3px);
    }

    .split-box-subtitle {
      color: #5A4A42;
      opacity: 0.9;
    }
  }

  // 未启用状态的样式
  &.disabled {
    background-color: #F5F5F5;
    cursor: not-allowed;
    opacity: 0.85;
    position: relative;
    overflow: hidden;

    &::before {
      display: none;
    }

    &:hover {
      transform: none;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

      .split-box-title {
        color: #2C2C2C;
        transform: none;
      }

      .split-box-subtitle {
        color: #666;
        opacity: 0.85;
      }
    }

    .split-box-image {
      filter: grayscale(50%);
      opacity: 0.7;
    }

    .split-box-title {
      color: #999;
    }

    .split-box-subtitle {
      color: #999;
    }
  }
  
  // 充能状态样式
  &.charging {
    cursor: pointer;
    opacity: 1;
    transition: box-shadow 0.3s ease;
    box-shadow: 0 0 30px rgba(255, 248, 232, 0.6),
                0 0 60px rgba(255, 248, 232, 0.3);
    
    .split-box-image {
      filter: grayscale(0%);
      opacity: 1;
      animation: pulse 0.8s ease-in-out infinite;
    }
    
    .split-box-title {
      color: #C3272B;
      animation: textGlow 0.8s ease-in-out infinite;
    }
    
    .split-box-subtitle {
      color: #666;
      opacity: 1;
    }
  }
  
  // 充能失败(中断)状态
  &.charging-failed {
    .charge-progress-bar {
      animation: energyDissipate 0.4s ease-out forwards;
    }
    
    .split-box-image {
      animation: shake 0.4s ease-in-out;
    }
    
    .split-box-title {
      animation: textFade 0.4s ease-out;
    }
  }
}

.split-box-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 25px;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  width: 100%;
}

.split-image-wrapper {
  width: 140px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.split-box-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 12px;
}

.split-image-placeholder {
  width: 100%;
  height: 100%;
  border: 2px dashed #AAA;
  border-radius: 12px;
  background-color: #FFFBF8;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  padding: 10px;
  text-align: center;

  .placeholder-text {
    font-size: 12px;
    color: #AAA;
    transition: color 0.5s ease;
  }
}

.split-box-title {
  font-size: 28px;
  font-weight: 700;
  color: #2C2C2C;
  margin: 0;
  letter-spacing: 6px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

.split-box-subtitle {
  font-size: 14px;
  font-weight: 400;
  color: #666;
  margin: 0;
  letter-spacing: 1px;
  line-height: 1.6;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
  max-width: 280px;
}

// 充能进度条
.charge-progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, 
    rgba(255, 248, 232, 0.95) 0%, 
    rgba(255, 248, 232, 0.85) 50%,
    rgba(255, 248, 232, 0.7) 100%);
  transition: height 0.05s linear;
  z-index: 1;
  pointer-events: none;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, 
      transparent 0%, 
      #FFF8E8 20%, 
      #FFFFFF 50%, 
      #FFF8E8 80%, 
      transparent 100%);
    box-shadow: 0 0 20px rgba(255, 248, 232, 0.8),
                0 0 40px rgba(255, 248, 232, 0.4);
    animation: shimmer 1s ease-in-out infinite;
  }
  
  // 充能中断时的消散动画
  &.failing {
    animation: progressDissipate 0.4s ease-out forwards;
    
    &::before {
      animation: edgeFade 0.4s ease-out forwards;
    }
  }
}

.charge-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: radial-gradient(ellipse at center bottom,
    rgba(255, 248, 232, 0.4) 0%,
    transparent 70%);
  animation: glowPulse 1s ease-in-out infinite;
}

// 充能提示文字
.charge-hint {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 32px;
  font-weight: 700;
  color: #C3272B;
  margin: 0;
  z-index: 10;
  text-shadow: 0 0 20px rgba(195, 39, 43, 0.5),
               0 0 40px rgba(195, 39, 43, 0.3);
  pointer-events: none;
}

// 充能动画
@keyframes shimmer {
  0%, 100% {
    opacity: 0.8;
    transform: scaleX(0.8);
  }
  50% {
    opacity: 1;
    transform: scaleX(1);
  }
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes textGlow {
  0%, 100% {
    text-shadow: 0 0 10px rgba(195, 39, 43, 0.3);
  }
  50% {
    text-shadow: 0 0 20px rgba(195, 39, 43, 0.6),
                 0 0 30px rgba(195, 39, 43, 0.4);
  }
}

@keyframes numberPulse {
  0%, 100% {
    transform: translateX(-50%) scale(1);
  }
  50% {
    transform: translateX(-50%) scale(1.1);
  }
}

// 充能中断动画 - 能量消散
@keyframes energyDissipate {
  0% {
    opacity: 1;
    filter: brightness(1) blur(0);
  }
  50% {
    opacity: 0.6;
    filter: brightness(1.5) blur(2px);
  }
  100% {
    opacity: 0;
    filter: brightness(2) blur(4px);
  }
}

@keyframes progressDissipate {
  0% {
    opacity: 1;
    transform: scaleY(1);
  }
  100% {
    opacity: 0;
    transform: scaleY(0.8);
    filter: blur(8px);
  }
}

@keyframes edgeFade {
  0% {
    opacity: 1;
    box-shadow: 0 0 20px rgba(255, 248, 232, 0.8),
                0 0 40px rgba(255, 248, 232, 0.4);
  }
  100% {
    opacity: 0;
    box-shadow: 0 0 0 transparent;
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px) rotate(-2deg);
  }
  75% {
    transform: translateX(4px) rotate(2deg);
  }
}

@keyframes textFade {
  0% {
    opacity: 1;
    transform: translateY(0);
  }
  100% {
    opacity: 0.5;
    transform: translateY(3px);
  }
}

// 动画效果
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 四个小盒子淡入上移动画
@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(40px) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .home-container {
    padding: 60px 40px;
  }

  .boxes-wrapper {
    gap: 40px;
  }

  .box {
    min-height: 420px;
    padding: 40px 30px;
  }

  .image-wrapper {
    width: 180px;
    height: 180px;
  }

  .box-title {
    font-size: 36px;
    letter-spacing: 8px;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 40px 24px;
  }

  .back-button {
    top: 20px;
    left: 20px;
    padding: 10px 18px;
    font-size: 14px;
  }

  .boxes-wrapper {
    flex-direction: column;
    gap: 30px;
  }

  .box {
    min-height: 350px;
    padding: 35px 25px;

    &:hover {
      transform: scale(1.12);
    }
  }

  // 移动端禁用缩小效果
  .boxes-wrapper:hover &:not(:hover) {
    opacity: 0.7;
    transform: scale(0.95);
    filter: brightness(0.95);
  }

  .image-wrapper {
    width: 150px;
    height: 150px;
  }

  .box-title {
    font-size: 32px;
    letter-spacing: 6px;
  }

  .box-subtitle {
    font-size: 14px;
  }

  // 展开后的四个小盒子 - 移动端改为单列
  .split-boxes-wrapper {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 10px;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }

  .split-box {
    min-height: 280px;
    padding: 25px 20px;
  }

  .split-image-wrapper {
    width: 120px;
    height: 120px;
  }

  .split-box-title {
    font-size: 24px;
    letter-spacing: 4px;
  }

  .split-box-subtitle {
    font-size: 13px;
    max-width: 240px;
  }
}
</style>
