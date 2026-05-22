<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useMemoStore } from '@/stores/memo.js'

const emit = defineEmits(['hide'])

const memoStore = useMemoStore()

const memoForm = reactive({
  text: ''
})

const errors = reactive({
  text: ''
})

const validateText = (value) => {
  if (!value || value.trim() === '') {
    errors.text = '内容不能为空'
    return false
  }
  if (value.length > 500) {
    errors.text = '内容过长（最多500字符）'
    return false
  }
  errors.text = ''
  return true
}

const handleInput = () => {
  validateText(memoForm.text)
}

const handleCancel = () => {
  emit('hide')
}

const handleOK = async () => {
  if (!validateText(memoForm.text)) {
    ElMessage.error('请检查表单填写是否正确')
    return
  }

  try {
    await memoStore.addImportantMemo({ text: memoForm.text.trim() })
    ElMessage.success('重要备忘已保存 ✧(≖ ◡ ≖✿)')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}
</script>

<template>
  <div class="memo-important">
    <div class="card-header">
      <h3 class="card-title">重要备忘 ★</h3>
    </div>
    <div class="card-content">
      <div class="memo-form">
        <div class="form-item full-width">
          <label class="form-label" for="importantMemoText">
            <span class="form-icon">★</span>
            <span class="form-name">记录内容</span>
          </label>
          <textarea
            id="importantMemoText"
            v-model="memoForm.text"
            @input="handleInput"
            placeholder="写下需要长期保留的重要事项..."
            class="form-input form-textarea"
            :class="{ 'has-error': errors.text }"
            rows="5"
          ></textarea>
          <div class="char-count">{{ memoForm.text.length }}/500</div>
          <span v-if="errors.text" class="error-message">{{ errors.text }}</span>
        </div>
      </div>
    </div>
    <button class="cancel-button" @click="handleCancel">不想记了</button>
    <button class="ok-button" @click="handleOK">OK</button>
  </div>
</template>

<style scoped lang="less">
.memo-important {
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
  flex-shrink: 0;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #c3272b;
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
  color: #5a4a42;
}

.form-icon {
  font-size: 16px;
}

.form-name {
  letter-spacing: 0.5px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #ffefd5;
  border-radius: 8px;
  background: #ffffff;
  font-size: 14px;
  font-family: inherit;
  color: #333333;
  outline: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
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
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(195, 39, 43, 0.3);
}
</style>
