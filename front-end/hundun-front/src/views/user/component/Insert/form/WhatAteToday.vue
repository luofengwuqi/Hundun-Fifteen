<script setup>
import { reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { insertFoodAPI, updateFoodAPI } from '@/api/food.js'

// 定义事件
const emit = defineEmits(['hide'])

// 表单数据
const mealRecord = reactive({
  morningFood: '',      // 早餐内容
  afternoonFood: '',    // 午餐内容
  eveningFood: '',      // 晚餐内容
  elseFood: ''          // 零食/其他
})

// 错误信息
const errors = reactive({
  morningFood: '',
  afternoonFood: '',
  eveningFood: '',
  elseFood: ''
})

// 验证单个字段
const validateField = (field, value) => {
  if (!value || value === '') {
    errors[field] = ''
    return true
  }

  // 这里可以添加具体的验证逻辑，比如长度限制等
  if (value.length > 100) {
    errors[field] = '输入内容过长（最多100字符）'
    return false
  }

  errors[field] = ''
  return true
}

// 输入处理
const handleInput = (field) => {
  validateField(field, mealRecord[field])
}

// 验证所有字段
const validateAll = () => {
  let isValid = true
  Object.keys(mealRecord).forEach(field => {
    if (!validateField(field, mealRecord[field])) {
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

  // 处理空字符串，转换为 null
  const dataToSend = {
    morningFood: mealRecord.morningFood || null,
    afternoonFood: mealRecord.afternoonFood || null,
    eveningFood: mealRecord.eveningFood || null,
    elseFood: mealRecord.elseFood || null
  }

  console.log('保存饮食记录:', dataToSend)
  try {
    const res = await insertFoodAPI(dataToSend)
    //res.data.code:1 成功 0 已存在
    if(res.data === 0){
      //已有记录 弹出警告框
// TODO: 警告框函数
      showWarning(dataToSend)
      return
    }
    ElMessage.success('保存成功(∠・ω< )⌒★')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
  }
}
// TODO: 警告框函数
const showWarning = (dto) => {
  ElMessageBox.confirm(
    `您今天似乎已经记录过了呢/w\\
    继续执行将覆盖先前的记录确定要继续吗?`,
    'Warning',
    {
      confirmButtonText: '你说得对',
      cancelButtonText: '啊错错错',
      type: 'warning',
    }
  )
    .then(() => {
      updateFoodAPI(dto)
      emit('hide')
      ElMessage({
        type: 'success',
        message: '执行成功(∠・ω< )⌒★',
      })
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消操作/w\\',
      })
    })
}
</script>

<template>
  <div class="what-ate-today">
    <div class="card-header">
      <h3 class="card-title">今天吃了什么(っˆڡˆς)</h3>
    </div>
    <div class="card-content">
      <div class="meal-form">
        <!-- 早餐 -->
        <div class="meal-item">
          <label class="meal-label" for="morningFood">
            <span class="meal-icon">🌅</span>
            <span class="meal-name">早餐</span>
          </label>
          <input
            id="morningFood"
            v-model="mealRecord.morningFood"
            @input="handleInput('morningFood')"
            type="text"
            placeholder="吃了什么？"
            class="meal-input"
            :class="{ 'has-error': errors.morningFood }"
          />
          <span v-if="errors.morningFood" class="error-message">{{ errors.morningFood }}</span>
        </div>

        <!-- 午餐 -->
        <div class="meal-item">
          <label class="meal-label" for="afternoonFood">
            <span class="meal-icon">☀️</span>
            <span class="meal-name">午餐</span>
          </label>
          <input
            id="afternoonFood"
            v-model="mealRecord.afternoonFood"
            @input="handleInput('afternoonFood')"
            type="text"
            placeholder="吃了什么？"
            class="meal-input"
            :class="{ 'has-error': errors.afternoonFood }"
          />
          <span v-if="errors.afternoonFood" class="error-message">{{ errors.afternoonFood }}</span>
        </div>

        <!-- 晚餐 -->
        <div class="meal-item">
          <label class="meal-label" for="eveningFood">
            <span class="meal-icon">🌙</span>
            <span class="meal-name">晚餐</span>
          </label>
          <input
            id="eveningFood"
            v-model="mealRecord.eveningFood"
            @input="handleInput('eveningFood')"
            type="text"
            placeholder="吃了什么？"
            class="meal-input"
            :class="{ 'has-error': errors.eveningFood }"
          />
          <span v-if="errors.eveningFood" class="error-message">{{ errors.eveningFood }}</span>
        </div>

        <!-- 其他 -->
        <div class="meal-item">
          <label class="meal-label" for="elseFood">
            <span class="meal-icon">✨</span>
            <span class="meal-name">其他</span>
          </label>
          <input
            id="elseFood"
            v-model="mealRecord.elseFood"
            @input="handleInput('elseFood')"
            type="text"
            placeholder="有没有吃夜宵?"
            class="meal-input"
            :class="{ 'has-error': errors.elseFood }"
          />
          <span v-if="errors.elseFood" class="error-message">{{ errors.elseFood }}</span>
        </div>
      </div>
    </div>
    <button class="cancel-button" @click="handleCancel">不想记了</button>
    <button class="ok-button" @click="handleOK">OK</button>
  </div>
</template>

<style scoped lang="less">
.what-ate-today {
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
