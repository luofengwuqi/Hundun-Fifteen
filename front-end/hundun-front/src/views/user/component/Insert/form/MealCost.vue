 <script setup>
import {  reactive } from 'vue'
import {  insertFoodMoneyAPI, updateFoodMoneyAPI } from '@/api/food.js'
import {  ElMessage, ElMessageBox } from 'element-plus'

// 定义事件
const emit = defineEmits(['hide'])

// 表单数据
const mealData = reactive({
  morningFoodPrice: '',
  afternoonFoodPrice: '',
  eveningFoodPrice: '',
  elseFoodPrice: ''
})

// 错误信息
const errors = reactive({
  morningFoodPrice: '',
  afternoonFoodPrice: '',
  eveningFoodPrice: '',
  elseFoodPrice: ''
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

  // 处理空字符串，转换为 null
  const dataToSend = {
    morningFoodPrice: mealData.morningFoodPrice || null,
    afternoonFoodPrice: mealData.afternoonFoodPrice || null,
    eveningFoodPrice: mealData.eveningFoodPrice || null,
    elseFoodPrice: mealData.elseFoodPrice || null
  }

  console.log('保存饮食花费记录:', dataToSend)
  try {
    const res = await insertFoodMoneyAPI(dataToSend)
    //res.data.code:1 成功 0 已存在
    if(res.data === 0){
      //已有记录 弹出警告框
      showWarning(dataToSend)
      return
    }
    ElMessage.success('保存成功(∠・ω< )⌒★')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
  }
}

//警告框函数
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
      updateFoodMoneyAPI(dto)
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
  <div class="meal-cost">
    <div class="card-header">
      <h3 class="card-title">吃饭花了多少ノ( º _ ºノ)</h3>
    </div>
    <div class="card-content">
      <div class="meal-form">
        <!-- 早餐 -->
        <div class="meal-item">
          <label class="meal-label" for="morningFoodPrice">
            <span class="meal-icon">🌅</span>
            <span class="meal-name">早餐</span>
          </label>
          <input
            id="morningFoodPrice"
            v-model="mealData.morningFoodPrice"
            @input="handleInput('morningFoodPrice')"
            type="text"
            placeholder="早餐消费"
            class="meal-input"
            :class="{ 'has-error': errors.morningFoodPrice }"
          />
          <span v-if="errors.morningFoodPrice" class="error-message">{{ errors.morningFoodPrice }}</span>
        </div>

        <!-- 午餐 -->
        <div class="meal-item">
          <label class="meal-label" for="afternoonFoodPrice">
            <span class="meal-icon">☀️</span>
            <span class="meal-name">午餐</span>
          </label>
          <input
            id="afternoonFoodPrice"
            v-model="mealData.afternoonFoodPrice"
            @input="handleInput('afternoonFoodPrice')"
            type="text"
            placeholder="午餐消费"
            class="meal-input"
            :class="{ 'has-error': errors.afternoonFoodPrice }"
          />
          <span v-if="errors.afternoonFoodPrice" class="error-message">{{ errors.afternoonFoodPrice }}</span>
        </div>

        <!-- 晚餐 -->
        <div class="meal-item">
          <label class="meal-label" for="eveningFoodPrice">
            <span class="meal-icon">🌙</span>
            <span class="meal-name">晚餐</span>
          </label>
          <input
            id="eveningFoodPrice"
            v-model="mealData.eveningFoodPrice"
            @input="handleInput('eveningFoodPrice')"
            type="text"
            placeholder="晚餐消费"
            class="meal-input"
            :class="{ 'has-error': errors.eveningFoodPrice }"
          />
          <span v-if="errors.eveningFoodPrice" class="error-message">{{ errors.eveningFoodPrice }}</span>
        </div>

        <!-- 其他 -->
        <div class="meal-item">
          <label class="meal-label" for="elseFoodPrice">
            <span class="meal-icon">✨</span>
            <span class="meal-name">其他</span>
          </label>
          <input
            id="elseFoodPrice"
            v-model="mealData.elseFoodPrice"
            @input="handleInput('elseFoodPrice')"
            type="text"
            placeholder="有没有吃夜宵?"
            class="meal-input"
            :class="{ 'has-error': errors.elseFoodPrice }"
          />
          <span v-if="errors.elseFoodPrice" class="error-message">{{ errors.elseFoodPrice }}</span>
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
  flex: 0 1 auto;
  padding: 8px 0 50px 0;
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
  transition: none;
}

.meal-label:hover {
  color: #C3272B;
}

.meal-icon {
  font-size: 16px;
  filter: grayscale(0.2);
  transition: none;
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
  transition: none;
  outline: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.meal-input::placeholder {
  color: #AAA;
  font-style: italic;
}





.meal-input.has-error {
  border-color: #E74C3C;
  background: #FDEDEC;
}



.error-message {
  font-size: 11px;
  color: #E74C3C;
  margin-top: -2px;
  animation: none;
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
