<script setup>
import {  reactive } from 'vue'

// 定义事件
const emit = defineEmits(['hide'])

// 表单数据
const mealData = reactive({
  breakfast: '',
  lunch: '',
  dinner: '',
  other: ''
})

// 错误信息
const errors = reactive({
  breakfast: '',
  lunch: '',
  dinner: '',
  other: ''
})

// 验证单个字段
const validateField = (field, value) => {
  if (!value || value === '') {
    errors[field] = ''
    return true
  }

  // 验证是否为数字且最多两位小数
  const pattern = /^\d+(\.\d{1,2})?$/
  if (!pattern.test(value)) {
    errors[field] = '请输入有效金额（最多两位小数）'
    return false
  }

  errors[field] = ''
  return true
}

// 输入处理
const handleInput = (field) => {
  validateField(field, mealData[field])
}

// 验证所有字段
const validateAll = () => {
  let isValid = true
  Object.keys(mealData).forEach(field => {
    if (!validateField(field, mealData[field])) {
      isValid = false
    }
  })
  return isValid
}

const handleCancel = () => {
  emit('hide')
}

const handleOK = async () => {
  if (!validateAll()) {
    return
  }

  // TODO: 调用后端接口保存数据
  console.log('保存饮食花费记录:', mealData)
  try {
    // const res = await saveMealCostAPI(mealData)
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
  }
}
</script>

<template>
  <div class="meal-cost">
    <div class="card-header">
      <h3 class="card-title">吃饭花了多少ノ( º _ ºノ)</h3>
    </div>
    <div class="card-content">
      <div class="meal-form">
        <!-- 早餐 -->
        <div class="meal-item">
          <label class="meal-label" for="breakfast">
            <span class="meal-icon">🌅</span>
            <span class="meal-name">早餐</span>
          </label>
          <input
            id="breakfast"
            v-model="mealData.breakfast"
            @input="handleInput('breakfast')"
            type="text"
            class="meal-input"
            :class="{ 'has-error': errors.breakfast }"
          />
          <span v-if="errors.breakfast" class="error-message">{{ errors.breakfast }}</span>
        </div>

        <!-- 午餐 -->
        <div class="meal-item">
          <label class="meal-label" for="lunch">
            <span class="meal-icon">☀️</span>
            <span class="meal-name">午餐</span>
          </label>
          <input
            id="lunch"
            v-model="mealData.lunch"
            @input="handleInput('lunch')"
            type="text"
            placeholder="0.00"
            class="meal-input"
            :class="{ 'has-error': errors.lunch }"
          />
          <span v-if="errors.lunch" class="error-message">{{ errors.lunch }}</span>
        </div>

        <!-- 晚餐 -->
        <div class="meal-item">
          <label class="meal-label" for="dinner">
            <span class="meal-icon">🌙</span>
            <span class="meal-name">晚餐</span>
          </label>
          <input
            id="dinner"
            v-model="mealData.dinner"
            @input="handleInput('dinner')"
            type="text"
            placeholder="0.00"
            class="meal-input"
            :class="{ 'has-error': errors.dinner }"
          />
          <span v-if="errors.dinner" class="error-message">{{ errors.dinner }}</span>
        </div>

        <!-- 其他 -->
        <div class="meal-item">
          <label class="meal-label" for="other">
            <span class="meal-icon">✨</span>
            <span class="meal-name">其他</span>
          </label>
          <input
            id="other"
            v-model="mealData.other"
            @input="handleInput('other')"
            type="text"
            placeholder="0.00"
            class="meal-input"
            :class="{ 'has-error': errors.other }"
          />
          <span v-if="errors.other" class="error-message">{{ errors.other }}</span>
        </div>
      </div>
    </div>
    <button class="cancel-button" @click="handleCancel">不想记了</button>
    <button class="ok-button" @click="handleOK">OK</button>
  </div>
</template>

<style scoped lang="less">
.meal-cost {
  width: 100%;
  height: 100%;
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }
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
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 8px 0;
  margin-top: -20px;
}

.meal-form {
  width: 100%;
  max-width: 500px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.meal-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
}

.meal-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #5A4A42;
  cursor: pointer;
  transition: all 0.2s ease;
}

.meal-label:hover {
  color: #C3272B;
}

.meal-icon {
  font-size: 16px;
  filter: grayscale(0.2);
  transition: transform 0.2s ease;
}

.meal-label:hover .meal-icon {
  transform: scale(1.15) rotate(-5deg);
}

.meal-name {
  letter-spacing: 0.5px;
}

.meal-input {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid #FFEFD5;
  border-radius: 8px;
  background: #FFFFFF;
  font-size: 14px;
  font-family: inherit;
  color: #333333;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.meal-input::placeholder {
  color: #AAA;
  font-style: italic;
}

.meal-input:hover {
  border-color: #FFD4A8;
  box-shadow: 0 2px 6px rgba(195, 39, 43, 0.08);
}

.meal-input:focus {
  border-color: #C3272B;
  box-shadow: 0 0 0 3px rgba(195, 39, 43, 0.1);
  background: #FFFBF8;
}

.meal-input.has-error {
  border-color: #E74C3C;
  background: #FDEDEC;
}

.meal-input.has-error:focus {
  box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1);
}

.error-message {
  font-size: 11px;
  color: #E74C3C;
  margin-top: -2px;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  transition: all 0.3s ease;

  &:hover {
    background: #E8E8E8;
    border-color: #CCC;
  }
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
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(195, 39, 43, 0.3);

  &:hover {
    background: #A82025;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(195, 39, 43, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}
</style>
