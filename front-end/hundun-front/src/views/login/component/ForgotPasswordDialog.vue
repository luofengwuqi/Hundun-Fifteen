<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addFeedbackAPI } from '@/api/feedback.js'

const dialogVisible = ref(false)
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  text: ''
})

const rules = {
  text: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 1, message: '反馈内容不能为空', trigger: 'blur' }
  ]
}

const open = () => {
  form.text = ''
  dialogVisible.value = true
}

defineExpose({ open })

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await addFeedbackAPI('登录问题', form.text)
      ElMessage.success('反馈已提交，我们会尽快处理 (∠・ω< )⌒★')
      dialogVisible.value = false
    } catch {
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <el-dialog v-model="dialogVisible" title="忘记密码" width="420px" :close-on-click-modal="false">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="auto">
      <el-form-item prop="text">
        <el-input
          v-model="form.text"
          type="textarea"
          :rows="5"
          placeholder="请输入您的邮箱或手机号以及问题描述，管理员将尽快为您处理"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading" style="background-color: #C3272B; border: none;">
        提交
      </el-button>
    </template>
  </el-dialog>
</template>
