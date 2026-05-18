<script setup>
import { reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useMemoStore } from '@/stores/memo.js'

// 定义事件
const emit = defineEmits(['hide'])

// 引入 memo store
const memoStore = useMemoStore()

// 表单数据
const memoForm = reactive({
  beginTime: '',
  endTime: '',
  text: '',
  status: 0 // 默认开启邮件通知
})

// 错误信息
const errors = reactive({
  beginTime: '',
  endTime: '',
  text: ''
})

// 验证单个字段
const validateField = (field, value) => {
  if (field === 'text') {
    if (!value || value.trim() === '') {
      errors[field] = '备忘内容不能为空'
      return false
    }
    if (value.length > 500) {
      errors[field] = '内容过长（最多500字符）'
      return false
    }
    errors[field] = ''
    return true
  }

  if (field === 'beginTime' || field === 'endTime') {
    if (!value) {
      errors[field] = '时间不能为空'
      return false
    }
    errors[field] = ''
    return true
  }

  errors[field] = ''
  return true
}

// 输入处理
const handleInput = (field) => {
  validateField(field, memoForm[field])
}

// 验证所有字段
const validateAll = () => {
  let isValid = true
  Object.keys(memoForm).forEach(field => {
    if (field !== 'status') {
      if (!validateField(field, memoForm[field])) {
        isValid = false
      }
    }
  })

  // 验证结束时间不能早于开始时间
  if (memoForm.beginTime && memoForm.endTime) {
    const begin = new Date(memoForm.beginTime)
    const end = new Date(memoForm.endTime)
    if (end <= begin) {
      errors.endTime = '结束时间必须晚于开始时间'
      isValid = false
    }
  }

  return isValid
}

const handleCancel = () => {
  emit('hide')
}

const handleOK = async () => {
  if (!validateAll()) {
    ElMessage.error('请检查表单填写是否正确')
    return
  }

  // 准备提交数据
  const dataToSend = {
    beginTime: memoForm.beginTime,
    endTime: memoForm.endTime,
    text: memoForm.text.trim(),
    status: memoForm.status
  }

  console.log('保存备忘录:', dataToSend)
  try {
    await memoStore.addCommonMemo(dataToSend)
    ElMessage.success('备忘录创建成功 ✧(≖ ◡ ≖✿)')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

// 切换通知状态
const toggleStatus = () => {
  memoForm.status = memoForm.status === 1 ? 0 : 1
}

// 计算通知状态文本
const statusText = computed(() => {
  return memoForm.status === 1 ? '开启' : '关闭'
})
</script>

<template>
  <div class="memo-common">
    <div class="card-header">
      <h3 class="card-title">整点计划(｡•̀ᴗ-)✧</h3>
    </div>
    <div class="card-content">
      <div class="memo-form">
        <!-- 时间选择行 -->
        <div class="time-row">
          <!-- 开始时间 -->
          <div class="time-item">
            <label class="form-label">
              <span class="form-icon">⏰</span>
              <span class="form-name">开始时间</span>
            </label>
            <el-date-picker
              v-model="memoForm.beginTime"
              @change="handleInput('beginTime')"
              type="datetime"
              placeholder="选择开始时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              class="form-input date-picker"
              :class="{ 'has-error': errors.beginTime }"
            />
            <span v-if="errors.beginTime" class="error-message">{{ errors.beginTime }}</span>
          </div>

          <!-- 结束时间 -->
          <div class="time-item">
            <label class="form-label">
              <span class="form-icon">⏰</span>
              <span class="form-name">结束时间</span>
            </label>
            <el-date-picker
              v-model="memoForm.endTime"
              @change="handleInput('endTime')"
              type="datetime"
              placeholder="选择结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              class="form-input date-picker"
              :class="{ 'has-error': errors.endTime }"
            />
            <span v-if="errors.endTime" class="error-message">{{ errors.endTime }}</span>
          </div>
        </div>

        <!-- 备忘内容 -->
        <div class="form-item full-width">
          <label class="form-label" for="memoText">
            <span class="form-icon">📝</span>
            <span class="form-name">备忘内容</span>
          </label>
          <textarea
            id="memoText"
            v-model="memoForm.text"
            @input="handleInput('text')"
            placeholder="在此输入备忘内容..."
            class="form-input form-textarea"
            :class="{ 'has-error': errors.text }"
            rows="4"
          ></textarea>
          <div class="char-count">{{ memoForm.text.length }}/500</div>
          <span v-if="errors.text" class="error-message">{{ errors.text }}</span>
        </div>

        <!-- 邮件通知开关 -->
        <div class="form-item full-width">
          <div class="toggle-wrapper">
            <label class="form-label">
              <span class="form-icon">📧</span>
              <span class="form-name">邮件通知</span>
            </label>
            <div class="toggle-container" @click="toggleStatus">
              <div class="toggle-switch" :class="{ 'is-on': memoForm.status === 1 }">
                <div class="toggle-slider"></div>
              </div>
              <span class="toggle-text">{{ statusText }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <button class="cancel-button" @click="handleCancel">不想记了</button>
    <button class="ok-button" @click="handleOK">OK</button>
  </div>
</template>

<style scoped lang="less">
.memo-common {
  width: 100%;
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: none;
  display: flex;
  flex-direction: column;
  position: relative;

}

.card-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #FFF8E8;
  flex-shrink: 0;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #C3272B;
  letter-spacing: 0.5px;
}

.card-content {
  padding: 8px 0 50px 0;
  flex: 1;
}

.memo-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

// 时间选择行
.time-row {
  display: flex;
  gap: 12px;

  .time-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
    position: relative;
  }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;

  &.full-width {
    width: 100%;
  }
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #5A4A42;
  cursor: pointer;
  transition: none;
}

.form-label:hover {
  color: #C3272B;
}

.form-icon {
  font-size: 16px;
  filter: grayscale(0.2);
  transition: none;
}



.form-name {
  letter-spacing: 0.5px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #FFEFD5;
  border-radius: 8px;
  background: #FFFFFF;
  font-size: 14px;
  font-family: inherit;
  color: #333333;
  transition: none;
  outline: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

}

// 日期选择器样式
:deep(.date-picker) {
  width: 100%;

  .el-input__wrapper {
    padding: 8px 12px;
    box-shadow: none;
    border: 2px solid #FFEFD5;
    border-radius: 8px;
    background: #FFFFFF;
    transition: none;
  }
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  line-height: 1.6;
}

.char-count {
  font-size: 11px;
  color: #999;
  text-align: right;
  margin-top: -2px;
}

.error-message {
  font-size: 11px;
  color: #E74C3C;
  margin-top: -2px;
  animation: none;
}



// 开关样式
.toggle-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #FFF8E8;
  border-radius: 8px;
  border: 2px solid #FFEFD5;
  cursor: pointer;
  transition: none;
}

.toggle-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toggle-switch {
  position: relative;
  width: 44px;
  height: 24px;
  background: #DDD;
  border-radius: 12px;
  transition: none;
  cursor: pointer;

  &.is-on {
    background: #C3272B;
  }
}

.toggle-slider {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: #FFFFFF;
  border-radius: 50%;
  transition: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}



.toggle-text {
  font-size: 12px;
  font-weight: 600;
  color: #5A4A42;
  min-width: 32px;
}

.cancel-button {
  position: absolute;
  bottom: 12px;
  left: 12px;
  padding: 6px 12px;
  background: #F5F5F5;
  color: #666;
  border: 1px solid #DDD;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: none;
}

.ok-button {
  position: absolute;
  bottom: 12px;
  right: 12px;
  padding: 6px 16px;
  background: #C3272B;
  color: #FFFFFF;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: none;
  box-shadow: 0 2px 8px rgba(195, 39, 43, 0.3);
}
</style>
