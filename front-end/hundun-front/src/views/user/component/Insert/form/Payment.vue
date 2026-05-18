<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

// 定义事件
const emit = defineEmits(['hide'])

// 商品列表 - 默认展示一组数据
const paymentList = reactive([
  {
    id: Date.now(),
    productName: '',
    productPrice: ''
  }
])

// 错误信息
const errors = reactive({})

// 验证单个字段
const validateField = (index, field, value) => {
  const key = `${index}_${field}`
  
  if (field === 'productName') {
    if (!value || value.trim() === '') {
      errors[key] = '商品名称不能为空'
      return false
    }
    if (value.length > 100) {
      errors[key] = '商品名称过长（最多100字符）'
      return false
    }
    errors[key] = ''
    return true
  }

  if (field === 'productPrice') {
    if (!value || value.trim() === '') {
      errors[key] = '商品价格不能为空'
      return false
    }
    // 验证是否为有效的数字
    const price = parseFloat(value)
    if (isNaN(price) || price < 0) {
      errors[key] = '请输入有效的价格'
      return false
    }
    errors[key] = ''
    return true
  }

  errors[key] = ''
  return true
}

// 输入处理
const handleInput = (index, field) => {
  validateField(index, field, paymentList[index][field])
}

// 验证所有字段
const validateAll = () => {
  let isValid = true
  paymentList.forEach((item, index) => {
    if (!validateField(index, 'productName', item.productName)) {
      isValid = false
    }
    if (!validateField(index, 'productPrice', item.productPrice)) {
      isValid = false
    }
  })
  return isValid
}

// 添加新的一组数据
const addPaymentItem = () => {
  paymentList.push({
    id: Date.now(),
    productName: '',
    productPrice: ''
  })
}

// 删除一组数据
const removePaymentItem = (index) => {
  if (paymentList.length <= 1) {
    ElMessage.warning('至少保留一组数据哦 (・ω< )')
    return
  }
  // 先清理对应的错误信息（使用当前的索引）
  const oldKeys = Object.keys(errors)
  oldKeys.forEach(key => {
    if (key.startsWith(`${index}_`)) {
      delete errors[key]
    }
  })
  // 然后删除数据
  paymentList.splice(index, 1)
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
  const dataToSend = paymentList.map(item => ({
    productName: item.productName.trim(),
    productPrice: parseFloat(item.productPrice)
  }))

  console.log('保存购物记录:', dataToSend)
  
  // TODO: 调用后端 API 保存数据
  try {
    // await savePaymentAPI(dataToSend)
    ElMessage.success('购物记录保存成功 ✧(≖ ◡ ≖✿)')
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}
</script>

<template>
  <div class="payment">
    <div class="card-header">
      <h3 class="card-title">买了点啥(っˆڡˆς)</h3>
    </div>
    <div class="card-content">
      <div class="payment-form">
        <!-- 动态渲染商品列表 -->
        <div v-for="(item, index) in paymentList" :key="item.id" class="payment-item-group">
          <!-- 右上角的 +/- 控件 -->
          <div class="item-controls">
            <button 
              type="button"
              class="control-btn remove-btn" 
              @click="removePaymentItem(index)"
              :disabled="paymentList.length <= 1"
              title="删除此项"
            >
              -
            </button>
            <button 
              type="button"
              class="control-btn add-btn" 
              @click="addPaymentItem"
              title="添加新项"
            >
              +
            </button>
          </div>

          <!-- 商品名称和价格在同行 -->
          <div class="form-row">
            <!-- 商品名称 -->
            <div class="form-item">
              <label class="form-label" :for="`productName_${index}`">
                <span class="form-icon">🛍️</span>
                <span class="form-name">商品名称</span>
              </label>
              <input
                :id="`productName_${index}`"
                v-model="item.productName"
                @input="handleInput(index, 'productName')"
                type="text"
                placeholder="输入商品名称"
                class="form-input"
                :class="{ 'has-error': errors[`${index}_productName`] }"
              />
              <span v-if="errors[`${index}_productName`]" class="error-message">{{ errors[`${index}_productName`] }}</span>
            </div>

            <!-- 商品价格 -->
            <div class="form-item">
              <label class="form-label" :for="`productPrice_${index}`">
                <span class="form-icon">💰</span>
                <span class="form-name">商品价格</span>
              </label>
              <input
                :id="`productPrice_${index}`"
                v-model="item.productPrice"
                @input="handleInput(index, 'productPrice')"
                type="number"
                placeholder="输入价格"
                step="0.01"
                min="0"
                class="form-input"
                :class="{ 'has-error': errors[`${index}_productPrice`] }"
              />
              <span v-if="errors[`${index}_productPrice`]" class="error-message">{{ errors[`${index}_productPrice`] }}</span>
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
.payment {
  width: 100%;
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
  padding: 8px 0 50px 0;
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.25);
    }
  }
}

.payment-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.payment-item-group {
  position: relative;
  padding: 12px;
  background: #FFFFFF;
  border-radius: 8px;
  border: 2px solid #FFEFD5;
  transition: all 0.3s ease;

  &:hover {
    border-color: #FFD4A8;
  }
}

.item-controls {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 6px;
  z-index: 10;
}

.control-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;

  &.add-btn {
    background: #C3272B;
    color: #FFFFFF;
    box-shadow: 0 2px 6px rgba(195, 39, 43, 0.3);

    &:hover {
      background: #A82025;
      transform: scale(1.1);
      box-shadow: 0 3px 10px rgba(195, 39, 43, 0.4);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  &.remove-btn {
    background: #F5F5F5;
    color: #666;
    border: 1px solid #DDD;

    &:hover:not(:disabled) {
      background: #E8E8E8;
      border-color: #CCC;
      transform: scale(1.1);
    }

    &:active:not(:disabled) {
      transform: scale(0.95);
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
  flex: 1;

  &:last-child {
    margin-bottom: 0;
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
  transition: all 0.2s ease;
}

.form-label:hover {
  color: #C3272B;
}

.form-icon {
  font-size: 16px;
  filter: grayscale(0.2);
  transition: transform 0.2s ease;
}

.form-label:hover .form-icon {
  transform: scale(1.15) rotate(-5deg);
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  &:hover {
    border-color: #FFD4A8;
    box-shadow: 0 2px 6px rgba(195, 39, 43, 0.08);
  }

  &:focus {
    border-color: #C3272B;
    box-shadow: 0 0 0 3px rgba(195, 39, 43, 0.1);
    background: #FFFBF8;
  }

  &.has-error {
    border-color: #E74C3C;
    background: #FDEDEC;

    &:focus {
      box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1);
    }
  }
}

.form-input::placeholder {
  color: #AAA;
  font-style: italic;
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
