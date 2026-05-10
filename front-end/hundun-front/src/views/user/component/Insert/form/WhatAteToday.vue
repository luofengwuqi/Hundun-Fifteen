<script setup>
// 定义事件
const emit = defineEmits(['hide'])

const handleCancel = () => {
  emit('hide')
}

const handleOK = async () => {
  // TODO: 调用后端接口保存数据
  console.log('TODO: 调用接口保存饮食记录')
  try {
    // const res = await saveMealRecordAPI(data)
    emit('hide')
  } catch (error) {
    console.error('保存失败:', error)
  }
}
</script>

<template>
  <div class="what-ate-today">
    <div class="card-header">
      <h3 class="card-title">今天吃了什么(っˆڡˆς)</h3>
    </div>
    <div class="card-content">
      <!-- TODO: 实现记录饮食内容的功能 -->
      <p class="todo-text">TODO: 在这里添加饮食记录表单或内容展示区域</p>
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
}

.todo-text {
  color: #999;
  font-size: 12px;
  font-style: italic;
  text-align: center;
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
