<script setup>
// 首页组件 - 录入与翻找功能入口
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import insertImage from '@/assets/img/user/home/insert.png'
import selectImage from '@/assets/img/user/home/select.png'
import healthImage from '@/assets/img/user/home/health.png'
import financeImage from '@/assets/img/user/home/finance.png'
import knowledgeImage from '@/assets/img/user/home/knowledge.png'
import memoImage from '@/assets/img/user/home/memo.png'

const router = useRouter()
const isExpanded = ref(false)
const isAnimating = ref(false) // 控制动画状态

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

// 四个分类的数据
const categories = [
  {
    id: 1,
    title: '健康',
    subtitle: '人是铁 饭是钢 一顿不吃饿得慌',
    image: healthImage,
    path: '/user/select/health'
  },
  {
    id: 2,
    title: '财务',
    subtitle: '金钱不是万能的 但没有钱是万万不能的',
    image: financeImage,
    path: '/user/select/finance'
  },
  {
    id: 3,
    title: '知识',
    subtitle: '芝士 就是力量',
    image: knowledgeImage,
    path: '/user/select/knowledge'
  },
  {
    id: 4,
    title: '备忘',
    subtitle: '活着不是为了不朽 而是为了创造不朽',
    image: memoImage,
    path: '/user/select/memo'
  }
]

// 处理小盒子点击事件
const handleCategoryClick = (category) => {
  if (category.path) {
    router.push(category.path)
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
          :style="{ animationDelay: `${index * 0.08}s` }"
          @click="handleCategoryClick(category)"
        >
          <div class="split-box-content">
            <div class="split-image-wrapper">
              <img :src="category.image" :alt="category.title" class="split-box-image" />
            </div>
            <h3 class="split-box-title">{{ category.title }}</h3>
            <p class="split-box-subtitle">{{ category.subtitle }}</p>
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
