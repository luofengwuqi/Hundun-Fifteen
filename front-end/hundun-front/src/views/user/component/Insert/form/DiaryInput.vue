<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { analyzeDiaryAPI } from '@/api/ai.js'

const emit = defineEmits(['back'])

const diaryText = ref('')
const analyzing = ref(false)
const result = ref('')
const showResult = ref(false)

const handleAnalyze = async () => {
  if (!diaryText.value.trim()) {
    ElMessage.warning('请先写点日记内容 (・ω< )')
    return
  }
  analyzing.value = true
  showResult.value = false
  try {
    const res = await analyzeDiaryAPI(diaryText.value)
    const parsed = JSON.parse(res.data)
    result.value = JSON.stringify(parsed, null, 2)
    showResult.value = true
  } catch (e) {
    ElMessage.error('分析失败，请稍后再试')
  } finally {
    analyzing.value = false
  }
}

const handleBack = () => {
  emit('back')
}
</script>

<template>
  <div class="diary-input-container">
    <div class="diary-header">
      <h2 class="diary-title">✍️ 日记 AI 模式</h2>
      <p class="diary-subtitle">随便写写今天发生了什么，AI 帮你自动分析 (｡•̀ᴗ-)✧</p>
    </div>

    <div class="diary-body">
      <el-input
        v-model="diaryText"
        type="textarea"
        placeholder="今天早上吃了两个包子和一碗粥，花了12块钱。下午去公园跑了3公里，心情不错。晚上要记得买牙膏。"
        maxlength="2000"
        show-word-limit
        class="diary-textarea"
      />

      <div class="diary-actions">
        <el-button type="danger" :loading="analyzing" @click="handleAnalyze" class="analyze-btn">
          {{ analyzing ? 'AI 分析中...' : '🚀 AI 分析' }}
        </el-button>
      </div>

      <Transition name="fade">
        <div v-if="showResult" class="result-panel">
          <h3 class="result-title">📊 分析结果</h3>
          <pre class="result-json">{{ result }}</pre>
        </div>
      </Transition>
    </div>
  </div>
</template>

<style scoped lang="less">
.diary-input-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.diary-header {
  text-align: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.diary-title {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 700;
  color: #C3272B;
  letter-spacing: 4px;
}

.diary-subtitle {
  margin: 0;
  font-size: 14px;
  color: #AAA;
}

.diary-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
}

.diary-textarea {
  flex: 1;
  min-height: 0;

  :deep(.el-textarea__inner) {
    font-size: 15px;
    line-height: 1.8;
    border-color: #FFEFD5;
    border-radius: 12px;
    padding: 16px;
    height: 100%;

    &:focus {
      border-color: #C3272B;
      box-shadow: 0 0 0 2px rgba(195, 39, 43, 0.1);
    }
  }

  :deep(.el-input__count) {
    background: transparent;
    bottom: 8px;
    right: 12px;
    font-size: 12px;
    color: #BBB;
  }
}

.diary-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-shrink: 0;
}

.analyze-btn {
  padding: 12px 40px;
  font-size: 16px;
  border-radius: 10px;
  letter-spacing: 2px;

  :deep(&) {
    --el-button-bg-color: #C3272B;
    --el-button-border-color: #C3272B;
    --el-button-hover-bg-color: #D64A4F;
    --el-button-hover-border-color: #D64A4F;
    --el-button-active-bg-color: #A81F22;
    --el-button-active-border-color: #A81F22;
  }
}

.result-panel {
  background: #FFF;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.result-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #5A4A42;
}

.result-json {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: #333;
  background: #FFF8E8;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
