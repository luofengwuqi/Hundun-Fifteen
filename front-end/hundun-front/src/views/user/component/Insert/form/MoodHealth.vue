<script setup>
import { reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { insertMoodAPI, updateMoodAPI } from '@/api/mood.js'

const emit = defineEmits(['hide'])

const MOOD_OPTIONS = [
  { value: 0, label: '很差', icon: '😫' },
  { value: 1, label: '差', icon: '😞' },
  { value: 2, label: '一般', icon: '😐' },
  { value: 3, label: '好', icon: '😊' },
  { value: 4, label: '很好', icon: '🥰' }
]

const moodForm = reactive({
  mood: null,
  note: ''
})

const errors = reactive({
  mood: '',
  note: ''
})

const validateMood = () => {
  if (moodForm.mood === null || moodForm.mood === '') {
    errors.mood = '请选择心情等级'
    return false
  }
  errors.mood = ''
  return true
}

const validateNote = () => {
  if (moodForm.note && moodForm.note.length > 500) {
    errors.note = '备注不能超过 500 字'
    return false
  }
  errors.note = ''
  return true
}

const validateAll = () => {
  const m = validateMood()
  const n = validateNote()
  return m && n
}

const buildDto = () => ({
  mood: moodForm.mood,
  note: moodForm.note || null
})

const handleCancel = () => {
  emit('hide')
}

const submitUpdate = async () => {
  await updateMoodAPI(buildDto())
  ElMessage.success('心情记录已更新 (´∀｀)♡')
  emit('hide')
}

const showOverwriteConfirm = () => {
  ElMessageBox.confirm(
    '今天已经记过心情了，继续将覆盖先前的记录，确定吗？',
    '提示',
    {
      confirmButtonText: '覆盖',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => submitUpdate())
    .catch(() => {
      ElMessage.info('已取消')
    })
}

const handleOK = async () => {
  if (!validateAll()) {
    ElMessage.error('请检查表单填写是否正确')
    return
  }

  try {
    const res = await insertMoodAPI(buildDto())
    if (res.data === 0) {
      showOverwriteConfirm()
      return
    }
    ElMessage.success('心情记录已保存 (´∀｀)♡')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}
</script>

<template>
  <div class="mood-health">
    <div class="card-header">
      <h3 class="card-title">心情如何 (｡･ω･｡)</h3>
    </div>
    <div class="card-content">
      <div class="mood-form">
        <div class="form-item">
          <label class="form-label">
            <span class="form-icon">💭</span>
            <span class="form-name">今天心情</span>
          </label>
          <div class="mood-options">
            <button
              v-for="opt in MOOD_OPTIONS"
              :key="opt.value"
              type="button"
              class="mood-btn"
              :class="{ active: moodForm.mood === opt.value }"
              @click="moodForm.mood = opt.value; validateMood()"
            >
              <span class="mood-icon">{{ opt.icon }}</span>
              <span>{{ opt.label }}</span>
            </button>
          </div>
          <span v-if="errors.mood" class="error-message">{{ errors.mood }}</span>
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="form-icon">✏️</span>
            <span class="form-name">随笔</span>
          </label>
          <el-input
            v-model="moodForm.note"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="写写今天的随笔吧…"
            @input="validateNote"
          />
          <span v-if="errors.note" class="error-message">{{ errors.note }}</span>
        </div>
      </div>
    </div>
    <button class="cancel-button" @click="handleCancel">不想记了</button>
    <button class="ok-button" @click="handleOK">OK</button>
  </div>
</template>

<style scoped lang="less">
.mood-health {
  width: 100%;
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  position: relative;
}

.card-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #fff8e8;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #c3272b;
}

.card-content {
  padding: 8px 0 50px;
}

.mood-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #5a4a42;
}

.mood-options {
  display: flex;
  gap: 6px;
}

.mood-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 2px;
  border: 2px solid #ffefd5;
  border-radius: 10px;
  background: #fff;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;

  &.active {
    border-color: rgba(195, 39, 43, 0.35);
    background: #ffefd5;
    color: #333;
    font-weight: 600;
  }
}

.mood-icon {
  font-size: 18px;
}

.error-message {
  font-size: 11px;
  color: #e74c3c;
}

.cancel-button {
  position: absolute;
  bottom: 12px;
  left: 12px;
  padding: 6px 12px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}

.ok-button {
  position: absolute;
  bottom: 12px;
  right: 12px;
  padding: 6px 16px;
  background: #c3272b;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(195, 39, 43, 0.3);
}
</style>
