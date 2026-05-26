<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMemoStore } from '@/stores/memo.js'
import MemoSectionHead from './MemoSectionHead.vue'
import CommonMemoCard from './cards/CommonMemoCard.vue'
import { useCommonMemoActions } from './composables/useCommonMemoActions.js'

const props = defineProps({
  loading: { type: Boolean, default: false },
  collapsed: { type: Boolean, default: false },
  expanded: { type: Boolean, default: false },
  bodyHidden: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle'])

const memoStore = useMemoStore()
const todoFilter = ref('all')

const TODO_FILTER_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: 'pending', label: '未完成' },
  { value: 'done', label: '已完成' }
]

const sortedCommonMemos = computed(() => {
  return [...memoStore.commonMemos].sort((a, b) => {
    const ta = new Date(a.beginTime || 0).getTime()
    const tb = new Date(b.beginTime || 0).getTime()
    return tb - ta
  })
})

const emptyHint = computed(() => {
  if (todoFilter.value === 'done') return '暂无已完成记录'
  if (todoFilter.value === 'pending') return '暂无未完成待办'
  return '暂无待办'
})

const countText = computed(() => `${sortedCommonMemos.value.length} 条`)

const loadCommonMemos = async () => {
  try {
    await memoStore.getCommonMemos(todoFilter.value)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载 To Do 失败')
    throw e
  }
}

watch(todoFilter, () => {
  loadCommonMemos()
})

const { updatingCommonId, handleComplete, handleToggleNotify } =
  useCommonMemoActions(todoFilter)

const handleDeleteCommon = async (memo) => {
  try {
    await ElMessageBox.confirm('确定删除这条 To Do 吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await memoStore.removeCommonMemo(memo.id)
    await loadCommonMemos()
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<template>
  <section
    class="memo-section memo-section--todo"
    :class="{ 'is-header-only': collapsed }"
  >
    <MemoSectionHead
      title="To Do List"
      :count-text="countText"
      :expanded="expanded"
      @toggle="emit('toggle')"
    />

    <Transition name="section-panel">
      <div v-show="!bodyHidden" class="section-body-wrap">
        <div class="todo-body">
          <div class="todo-main">
            <div v-if="loading" class="section-state">加载中…</div>
            <div v-else-if="sortedCommonMemos.length === 0" class="section-state">
              <p>{{ emptyHint }}</p>
              <small>可在「录入」中添加备忘录</small>
            </div>
            <div v-else class="card-scroll">
              <CommonMemoCard
                v-for="memo in sortedCommonMemos"
                :key="memo.id"
                :memo="memo"
                :updating="updatingCommonId === memo.id"
                @delete="handleDeleteCommon(memo)"
                @complete="handleComplete(memo)"
                @toggle-notify="handleToggleNotify(memo)"
              />
            </div>
          </div>

          <aside class="todo-filter">
            <h4 class="filter-title">筛选</h4>
            <div class="filter-chips">
              <button
                v-for="opt in TODO_FILTER_OPTIONS"
                :key="opt.value"
                type="button"
                class="chip"
                :class="{ active: todoFilter === opt.value }"
                @click="todoFilter = opt.value"
              >
                {{ opt.label }}
              </button>
            </div>
          </aside>
        </div>
      </div>
    </Transition>
  </section>
</template>

<style scoped lang="less">
.todo-body {
  flex: 1 1 0%;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr minmax(140px, 180px);
  gap: 12px;
  align-items: stretch;
  overflow: visible;
}

.todo-main {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.todo-filter {
  margin: 2px 2px 2px 0;
  padding: 16px 14px;
  background: var(--box-mi-bai);
  border: 1px solid var(--border);
  border-radius: 10px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(195, 39, 43, 0.04);
}

.filter-chips {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chip {
  padding: 9px 14px;
  border: 2px solid transparent;
  border-radius: 10px;
  background: var(--bg);
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
  box-shadow: 0 1px 4px rgba(195, 39, 43, 0.04);

  &:hover {
    color: var(--text);
    border-color: var(--border);
  }

  &.active {
    background: var(--bg-active);
    border-color: rgba(195, 39, 43, 0.15);
    color: var(--text);
    font-weight: 600;
  }
}

@media (max-width: 720px) {
  .todo-body {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr auto;
    gap: 10px;
  }

  .todo-filter {
    margin: 0;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    padding: 14px 16px;
    border-radius: 10px;
  }

  .filter-chips {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>
