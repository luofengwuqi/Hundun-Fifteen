<script setup>
import { ElMessage } from 'element-plus'
import healthImg from '@/assets/img/user/home/health.png'
import financeImg from '@/assets/img/user/home/finance.png'
import knowledgeImg from '@/assets/img/user/home/knowledge.png'
import memoImg from '@/assets/img/user/home/memo.png'
import musicImg from '@/assets/img/music/music_icon.png'

const props = defineProps({ modelValue: { type: Object, required: true } })
const emit = defineEmits(['update:modelValue'])

const modules = [
  { key: 'health', label: '健康', img: healthImg, required: true },
  { key: 'finance', label: '财务', img: financeImg, required: true },
  { key: 'knowledge', label: '社区', img: knowledgeImg, required: false },
  { key: 'memo', label: '备忘', img: memoImg, required: false },
  { key: 'music', label: '音乐', img: musicImg, required: false }
]

const toggle = (mod) => {
  if (mod.required && props.modelValue[mod.key] === 1) {
    ElMessage.info({ message: '这个不能删哦', duration: 1500 })
    return
  }
  const newVal = props.modelValue[mod.key] === 1 ? 0 : 1
  emit('update:modelValue', { ...props.modelValue, [mod.key]: newVal })
}
</script>

<template>
  <div class="page">
    <h2 class="title">已经有了这些功能呢</h2>
    <p class="subtitle">看看有没有不需要的？</p>

    <div class="module-grid">
      <div
        v-for="mod in modules"
        :key="mod.key"
        class="module-card"
        :class="{ checked: modelValue[mod.key] === 1, required: mod.required }"
        @click="toggle(mod)"
      >
        <img :src="mod.img" :alt="mod.label" class="module-img" />
        <span class="module-label">{{ mod.label }}</span>
        <div class="check-box">
          <span v-if="modelValue[mod.key] === 1" class="check-mark">✓</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@red: #C3272B;
@cream: #FFF8E8;
@cream-dark: #FFEFD5;
@text: #5C3D2E;
@text-muted: #8B7355;

.page {
  text-align: center;
  width: 100%;
  padding: 10px 0;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: @text;
  margin: 0 0 4px;
}

.subtitle {
  font-size: 14px;
  color: @text-muted;
  margin: 0 0 20px;
}

.module-grid {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.module-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100px;
  padding: 14px 8px 10px;
  border-radius: 16px;
  background: #fff;
  border: 2px solid #eee;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;

  &:hover {
    border-color: @cream-dark;
    box-shadow: 0 4px 12px rgba(92, 61, 46, 0.08);
  }

  &.checked {
    border-color: @red;
    background: #fff;

    .module-img {
      filter: none;
    }

    .check-box {
      background: @red;
      border-color: @red;
    }
  }

  &:not(.checked) {
    .module-img {
      filter: grayscale(1) opacity(0.5);
    }

    .module-label {
      color: @text-muted;
    }
  }

  &.required .check-box {
    cursor: not-allowed;
  }
}

.module-img {
  width: 44px;
  height: 44px;
  object-fit: contain;
  transition: filter 0.25s;
}

.module-label {
  font-size: 13px;
  color: @text;
  font-weight: 500;
  transition: color 0.25s;
}

.check-box {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s;

  .check-mark {
    color: #fff;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
  }
}
</style>
