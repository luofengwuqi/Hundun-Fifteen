<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { insertPlaceHealthAPI, updatePlaceHealthAPI, insertPlaceFinanceAPI, updatePlaceFinanceAPI } from '@/api/place.js'

const emit = defineEmits(['hide'])

const TRANSPORT_OPTIONS = [
  { value: '步行', icon: '🚶' },
  { value: '自行车', icon: '🚲' },
  { value: '公交', icon: '🚌' },
  { value: '地铁', icon: '🚇' },
  { value: '私家车', icon: '🚗' },
  { value: '其他', icon: '🛴' }
]

const placeForm = reactive({
  location: '',
  transportMode: null,
  steps: 0,
  transportCost: null
})

const hasTransportCost = computed(() => {
  return placeForm.transportCost !== null && placeForm.transportCost !== ''
})

const errors = reactive({
  location: '',
  steps: '',
  transportCost: ''
})

const validateLocation = () => {
  if (!placeForm.location || !placeForm.location.trim()) {
    errors.location = '请填写去了哪里'
    return false
  }
  errors.location = ''
  return true
}

const validateSteps = () => {
  const s = Number(placeForm.steps) || 0
  if (s < 0 || s > 100000) {
    errors.steps = '步数须在 0~100000 之间'
    return false
  }
  errors.steps = ''
  return true
}

const validateTransportCost = () => {
  if (placeForm.transportCost !== null && placeForm.transportCost !== '') {
    const c = Number(placeForm.transportCost)
    if (isNaN(c) || c < 0) {
      errors.transportCost = '请输入有效的金额'
      return false
    }
  }
  errors.transportCost = ''
  return true
}

const validateAll = () => {
  const l = validateLocation()
  const s = validateSteps()
  const c = validateTransportCost()
  return l && s && c
}

const buildHealthDto = () => ({
  location: placeForm.location.trim(),
  transportMode: placeForm.transportMode,
  steps: Number(placeForm.steps) || 0
})

const buildFinanceDto = () => ({
  transportCost: hasTransportCost.value ? Number(placeForm.transportCost) : null,
  transportMode: placeForm.transportMode,
  location: placeForm.location.trim()
})

const handleCancel = () => {
  emit('hide')
}

const existed = reactive({
  health: false,
  finance: false
})

const submitUpdate = async () => {
  await updatePlaceHealthAPI(buildHealthDto())
  if (hasTransportCost.value) {
    if (existed.finance) {
      await updatePlaceFinanceAPI(buildFinanceDto())
    } else {
      await insertPlaceFinanceAPI(buildFinanceDto())
    }
  }
  ElMessage.success('外出记录已更新 (´∀｀)♡')
  emit('hide')
}

const showOverwriteConfirm = () => {
  ElMessageBox.confirm(
    '今天已经记过去哪里了，继续将覆盖先前的记录，确定吗？',
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
    const healthRes = await insertPlaceHealthAPI(buildHealthDto())
    existed.health = healthRes.data === 0

    if (hasTransportCost.value) {
      const financeRes = await insertPlaceFinanceAPI(buildFinanceDto())
      existed.finance = financeRes.data === 0
    }

    if (existed.health || existed.finance) {
      showOverwriteConfirm()
      return
    }

    ElMessage.success('外出记录已保存 (´∀｀)♡')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}
</script>

<template>
  <div class="place-where">
    <div class="card-header">
      <h3 class="card-title">去了哪里 ( ˘ ³˘)📍</h3>
    </div>
    <div class="card-content">
      <div class="place-form">
        <div class="form-item">
          <label class="form-label">
            <span class="form-icon">📍</span>
            <span class="form-name">地点</span>
          </label>
          <el-input
            v-model="placeForm.location"
            placeholder="今天去了哪里…"
            maxlength="100"
            @input="validateLocation"
          />
          <span v-if="errors.location" class="error-message">{{ errors.location }}</span>
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="form-icon">🚗</span>
            <span class="form-name">交通方式</span>
          </label>
          <div class="transport-options">
            <button
              v-for="opt in TRANSPORT_OPTIONS"
              :key="opt.value"
              type="button"
              class="transport-btn"
              :class="{ active: placeForm.transportMode === opt.value }"
              @click="placeForm.transportMode = opt.value"
            >
              <span class="transport-icon">{{ opt.icon }}</span>
              <span class="transport-label">{{ opt.value }}</span>
            </button>
          </div>
        </div>

        <div class="form-row">
          <div class="form-item form-item-half">
            <label class="form-label">
              <span class="form-icon">👣</span>
              <span class="form-name">步数</span>
            </label>
            <el-input-number
              v-model="placeForm.steps"
              :min="0"
              :max="100000"
              controls-position="right"
              @change="validateSteps"
            />
            <span v-if="errors.steps" class="error-message">{{ errors.steps }}</span>
          </div>

          <div class="form-item form-item-half">
            <label class="form-label">
              <span class="form-icon">💰</span>
              <span class="form-name">交通费</span>
            </label>
            <el-input
              v-model="placeForm.transportCost"
              placeholder="选填"
              @input="validateTransportCost"
            >
              <template #suffix>元</template>
            </el-input>
            <span v-if="errors.transportCost" class="error-message">{{ errors.transportCost }}</span>
          </div>
        </div>
      </div>
    </div>
    <button class="cancel-button" @click="handleCancel">不记了</button>
    <button class="ok-button" @click="handleOK">OK</button>
  </div>
</template>

<style scoped lang="less">
.place-where {
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

.place-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-item-half {
  flex: 1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #5a4a42;
}

.transport-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.transport-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 8px 4px;
  border: 2px solid #ffefd5;
  border-radius: 10px;
  background: #fff;
  font-size: 11px;
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

.transport-icon {
  font-size: 18px;
}

.transport-label {
  white-space: nowrap;
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
