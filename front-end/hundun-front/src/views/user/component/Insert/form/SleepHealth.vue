<script setup>
import { computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { insertSleepAPI, updateSleepAPI } from '@/api/sleep.js'

const emit = defineEmits(['hide'])

const QUALITY_OPTIONS = [
  { value: 0, label: '差', icon: '😫' },
  { value: 1, label: '一般', icon: '😐' },
  { value: 2, label: '好', icon: '😴' }
]

const sleepForm = reactive({
  quality: null,
  sleepHours: 0,
  sleepMinutes: 0
})

const errors = reactive({
  quality: '',
  duration: ''
})

const durationMin = computed(() => {
  const h = Number(sleepForm.sleepHours) || 0
  const m = Number(sleepForm.sleepMinutes) || 0
  return h * 60 + m
})

const validateQuality = () => {
  if (sleepForm.quality === null || sleepForm.quality === '') {
    errors.quality = '请选择睡眠质量'
    return false
  }
  errors.quality = ''
  return true
}

const validateDuration = () => {
  const total = durationMin.value
  if (total <= 0) {
    errors.duration = '请填写睡眠时长'
    return false
  }
  if (total > 1440) {
    errors.duration = '时长不能超过 24 小时'
    return false
  }
  errors.duration = ''
  return true
}

const validateAll = () => {
  const q = validateQuality()
  const d = validateDuration()
  return q && d
}

const buildDto = () => ({
  quality: sleepForm.quality,
  durationMin: durationMin.value
})

const handleCancel = () => {
  emit('hide')
}

const submitUpdate = async () => {
  await updateSleepAPI(buildDto())
  ElMessage.success('睡眠记录已更新 (´∀｀)♡')
  emit('hide')
}

const showOverwriteConfirm = () => {
  ElMessageBox.confirm(
    '今天已经记过睡眠了，继续将覆盖先前的记录，确定吗？',
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
    const res = await insertSleepAPI(buildDto())
    if (res.data === 0) {
      showOverwriteConfirm()
      return
    }
    ElMessage.success('睡眠记录已保存 (´∀｀)♡')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}
</script>

<template>
  <div class="sleep-health">
    <div class="card-header">
      <h3 class="card-title">睡得咋样 (´-ω-`)</h3>
    </div>
    <div class="card-content">
      <div class="sleep-form">
        <div class="form-item">
          <label class="form-label">
            <span class="form-icon">🌙</span>
            <span class="form-name">睡眠质量</span>
          </label>
          <div class="quality-options">
            <button
              v-for="opt in QUALITY_OPTIONS"
              :key="opt.value"
              type="button"
              class="quality-btn"
              :class="{ active: sleepForm.quality === opt.value }"
              @click="sleepForm.quality = opt.value; validateQuality()"
            >
              <span class="quality-icon">{{ opt.icon }}</span>
              <span>{{ opt.label }}</span>
            </button>
          </div>
          <span v-if="errors.quality" class="error-message">{{ errors.quality }}</span>
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="form-icon">⏱</span>
            <span class="form-name">睡眠时长</span>
          </label>
          <div class="duration-row">
            <div class="duration-field">
              <el-input-number
                v-model="sleepForm.sleepHours"
                :min="0"
                :max="24"
                controls-position="right"
                @change="validateDuration"
              />
              <span class="duration-unit">小时</span>
            </div>
            <div class="duration-field">
              <el-input-number
                v-model="sleepForm.sleepMinutes"
                :min="0"
                :max="59"
                controls-position="right"
                @change="validateDuration"
              />
              <span class="duration-unit">分钟</span>
            </div>
          </div>
          <p class="duration-hint">合计 {{ durationMin }} 分钟</p>
          <span v-if="errors.duration" class="error-message">{{ errors.duration }}</span>
        </div>
      </div>
    </div>
    <button class="cancel-button" @click="handleCancel">不想记了</button>
    <button class="ok-button" @click="handleOK">OK</button>
  </div>
</template>

<style scoped lang="less">
.sleep-health {
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

.sleep-form {
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

.quality-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quality-btn {
  flex: 1;
  min-width: 72px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  border: 2px solid #ffefd5;
  border-radius: 10px;
  background: #fff;
  font-size: 13px;
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

.quality-icon {
  font-size: 20px;
}

.duration-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.duration-field {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 120px;
}

.duration-unit {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
}

.duration-hint {
  margin: 0;
  font-size: 12px;
  color: #999;
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
