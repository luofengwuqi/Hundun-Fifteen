<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useMemoStore } from '@/stores/memo.js'
import { useMemoPanelFocus } from './memo/composables/useMemoPanelFocus.js'
import MemoTodoPanel from './memo/MemoTodoPanel.vue'
import MemoImportantPanel from './memo/MemoImportantPanel.vue'

const memoStore = useMemoStore()
const loading = ref(false)

const {
  toggleTodo,
  toggleImportant,
  isTodoFocused,
  isImportantFocused,
  isTodoCollapsed,
  isImportantCollapsed,
  focusClasses
} = useMemoPanelFocus()

const loadMemos = async () => {
  loading.value = true
  try {
    await Promise.all([
      memoStore.getCommonMemos('all'),
      memoStore.getImportantMemos()
    ])
  } catch (e) {
    console.error(e)
    if (!memoStore.commonMemos.length) {
      ElMessage.error('加载备忘录失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMemos()
})
</script>

<template>
  <div class="memo-select select-panel" :class="focusClasses">
    <MemoTodoPanel
      :loading="loading"
      :collapsed="isTodoCollapsed"
      :expanded="isTodoFocused"
      :body-hidden="isTodoCollapsed"
      @toggle="toggleTodo"
    />
    <MemoImportantPanel
      :loading="loading"
      :collapsed="isImportantCollapsed"
      :expanded="isImportantFocused"
      :body-hidden="isImportantCollapsed"
      @toggle="toggleImportant"
    />
  </div>
</template>

<style lang="less">
@import './memo/memo-shared.less';
</style>
