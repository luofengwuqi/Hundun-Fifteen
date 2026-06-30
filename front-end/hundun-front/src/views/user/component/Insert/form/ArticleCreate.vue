<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { submitArticleAPI } from '@/api/article'

const emit = defineEmits(['hide'])
const userStore = useUserStore()

const articleForm = reactive({
  title: '',
  creator: userStore.userInfo?.nickname || userStore.userInfo?.username || '',
  belonging: 2,
  status: 1,
  text: ''
})

const errors = reactive({
  title: '',
  creator: '',
  text: ''
})

const belongingOptions = [
  { value: 1, label: '帮助文档' },
  { value: 2, label: '知识分享' }
]

const statusOptions = [
  { value: 1, label: '公开' },
  { value: 2, label: '私密' }
]

const validateField = (field) => {
  const value = articleForm[field]
  if (field === 'title') {
    if (!value || value.trim() === '') {
      errors.title = '请输入文章标题'
      return false
    }
    if (value.length > 100) {
      errors.title = '标题不能超过 100 字'
      return false
    }
    errors.title = ''
    return true
  }
  if (field === 'creator') {
    if (!value || value.trim() === '') {
      errors.creator = '请输入创建者'
      return false
    }
    if (value.length > 50) {
      errors.creator = '创建者不能超过 50 字'
      return false
    }
    errors.creator = ''
    return true
  }
  if (field === 'text') {
    if (!value || value.trim() === '') {
      errors.text = '请输入文章正文'
      return false
    }
    errors.text = ''
    return true
  }
  return true
}

const handleInput = (field) => {
  validateField(field)
}

const validateAll = () => {
  const t = validateField('title')
  const c = validateField('creator')
  const x = validateField('text')
  return t && c && x
}

const handleCancel = () => {
  emit('hide')
}

const handleSubmit = async () => {
  if (!validateAll()) {
    ElMessage.error('请检查表单填写是否正确')
    return
  }

  try {
    await submitArticleAPI({
      title: articleForm.title.trim(),
      creator: articleForm.creator.trim(),
      belonging: articleForm.belonging,
      status: articleForm.status,
      text: articleForm.text.trim()
    })
    ElMessage.success('文章发布成功 🧀✨')
    emit('hide')
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请稍后重试')
  }
}
</script>

<template>
  <div class="article-create">
    <div class="article-header">
      <div class="header-accent" />
      <div class="header-icon-wrap">
        <span class="header-icon">🧀</span>
      </div>
      <div class="header-text">
        <h2 class="header-title">今日芝士</h2>
        <p class="header-desc">分享你的知识，让思想流动起来 ✦</p>
      </div>
    </div>

    <div class="article-body">
      <!-- 标题 + 创建者 -->
      <div class="form-row title-creator-row">
        <div class="form-group title-group">
          <label class="form-label">
            <span class="label-icon">📝</span>
            <span class="label-text">标题</span>
          </label>
          <input
            v-model="articleForm.title"
            @input="handleInput('title')"
            type="text"
            placeholder="给你的文章一个好标题"
            class="form-input"
            :class="{ 'has-error': errors.title }"
          />
          <span v-if="errors.title" class="error-msg">{{ errors.title }}</span>
        </div>

        <div class="form-group creator-group">
          <label class="form-label">
            <span class="label-icon">👤</span>
            <span class="label-text">创建者</span>
          </label>
          <input
            v-model="articleForm.creator"
            @input="handleInput('creator')"
            type="text"
            placeholder="你的名字"
            class="form-input"
            :class="{ 'has-error': errors.creator }"
          />
          <span v-if="errors.creator" class="error-msg">{{ errors.creator }}</span>
        </div>
      </div>

      <!-- 所属模块 + 是否公开 -->
      <div class="form-row">
        <div class="form-group row-half">
          <label class="form-label">
            <span class="label-icon">📂</span>
            <span class="label-text">所属模块</span>
          </label>
          <el-select
            v-model="articleForm.belonging"
            placeholder="选择所属模块"
            class="form-select"
          >
            <el-option
              v-for="opt in belongingOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>

        <div class="form-group row-half">
          <label class="form-label">
            <span class="label-icon">🔒</span>
            <span class="label-text">是否公开</span>
          </label>
          <div class="option-group">
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              type="button"
              class="option-btn"
              :class="{ active: articleForm.status === opt.value }"
              @click="articleForm.status = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- 正文 -->
      <div class="form-group body-group">
        <label class="form-label">
          <span class="label-icon">📄</span>
          <span class="label-text">正文</span>
        </label>
        <textarea
          v-model="articleForm.text"
          @input="handleInput('text')"
          placeholder="开始书写你的知识..."
          class="form-input body-textarea"
          :class="{ 'has-error': errors.text }"
        />
        <span v-if="errors.text" class="error-msg">{{ errors.text }}</span>
      </div>
    </div>

    <div class="article-footer">
      <button class="cancel-btn" @click="handleCancel">取消</button>
      <button class="submit-btn" @click="handleSubmit">发布文章</button>
    </div>
  </div>
</template>

<style scoped lang="less">
.article-create {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;
  animation: articleFadeIn 0.4s ease-out;
  box-shadow: 0 2px 12px rgba(212, 160, 23, 0.08);
}

.article-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 24px;
  border-bottom: 2px solid #FFF8E8;
  position: relative;
}

.header-accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #D4A017 0%, #E8C44A 100%);
  border-radius: 0 2px 2px 0;
}

.header-icon-wrap {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF8E8 0%, #FFF3D6 100%);
  border-radius: 10px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(212, 160, 23, 0.15);
}

.header-icon {
  font-size: 24px;
  line-height: 1;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #2C2C2C;
  letter-spacing: 2px;
}

.header-desc {
  margin: 0;
  font-size: 13px;
  color: #AAA;
  letter-spacing: 1px;
}

.article-body {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.12);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.2);
    }
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}

.form-row {
  display: flex;
  gap: 16px;
}

.title-creator-row {
  .title-group {
    flex: 2;
  }

  .creator-group {
    flex: 1;
  }
}

.row-half {
  flex: 1;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #5A4A42;
}

.label-icon {
  font-size: 16px;
  line-height: 1;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #FFEFD5;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  background: #FFFBF8;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
  outline: none;
  box-sizing: border-box;

  &::placeholder {
    color: #BBB;
  }

  &:focus {
    border-color: #D4A017;
    box-shadow: 0 0 0 3px rgba(212, 160, 23, 0.1);
  }

  &.has-error {
    border-color: #E74C3C;

    &:focus {
      box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1);
    }
  }
}

.form-select {
  width: 100%;

  :deep(.el-select__wrapper) {
    border: 2px solid #FFEFD5;
    border-radius: 8px;
    background: #FFFBF8;
    box-shadow: none;
    padding: 4px 14px;
    min-height: 40px;
    transition: border-color 0.25s ease, box-shadow 0.25s ease;

    &:hover {
      border-color: #E8C44A;
    }

    &.is-focused {
      border-color: #D4A017;
      box-shadow: 0 0 0 3px rgba(212, 160, 23, 0.1);
    }
  }

  :deep(.el-select__placeholder) {
    color: #BBB;
    font-size: 14px;
  }

  :deep(.el-select__selected-item) {
    font-size: 14px;
    color: #333;
  }
}

.option-group {
  display: flex;
  gap: 8px;
}

.option-btn {
  flex: 1;
  padding: 8px 16px;
  border: 2px solid #FFEFD5;
  border-radius: 8px;
  background: #FFFBF8;
  color: #5A4A42;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    border-color: #E8C44A;
    background: #FFF8E8;
  }

  &.active {
    border-color: #D4A017;
    background: linear-gradient(135deg, #FFF3D6 0%, #FFE8A0 100%);
    color: #5A3E00;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(212, 160, 23, 0.2);
  }
}

.body-group {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.body-textarea {
  flex: 1;
  min-height: 180px;
  resize: none;
  line-height: 1.7;
  font-size: 14px;
  padding: 14px;
}

.error-msg {
  font-size: 12px;
  color: #E74C3C;
  margin-top: 2px;
}

.article-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-top: 2px solid #FFF8E8;
  flex-shrink: 0;
}

.cancel-btn {
  padding: 8px 20px;
  border: 2px solid #FFEFD5;
  border-radius: 8px;
  background: #F5F5F5;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    border-color: #DDD;
    background: #EAEAEA;
    color: #444;
  }
}

.submit-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #C3272B 0%, #D64A4F 100%);
  color: #FFF;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  letter-spacing: 1px;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(195, 39, 43, 0.3);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 14px rgba(195, 39, 43, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

@keyframes articleFadeIn {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
