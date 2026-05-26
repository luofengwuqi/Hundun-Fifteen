<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMemoStore } from '@/stores/memo.js'
import MemoSectionHead from './MemoSectionHead.vue'
import ImportantMemoCard from './cards/ImportantMemoCard.vue'

const props = defineProps({
  loading: { type: Boolean, default: false },
  collapsed: { type: Boolean, default: false },
  expanded: { type: Boolean, default: false },
  bodyHidden: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle'])

const memoStore = useMemoStore()
const importantSearchKeyword = ref('')

const sortedImportantMemos = computed(() => {
  return [...memoStore.importantMemos].sort((a, b) => {
    const ta = new Date(a.createdAt || 0).getTime()
    const tb = new Date(b.createdAt || 0).getTime()
    return tb - ta
  })
})

const importantSearchActive = computed(
  () => importantSearchKeyword.value.trim().length > 0
)

const matchesImportantKeyword = (text, keyword) => {
  if (!keyword) return true
  const hay = (text ?? '').toString().toLowerCase()
  const needle = keyword.toLowerCase()
  return hay.includes(needle)
}

const filteredImportantMemos = computed(() => {
  const keyword = importantSearchKeyword.value.trim()
  if (!keyword) return sortedImportantMemos.value
  return sortedImportantMemos.value.filter((memo) =>
    matchesImportantKeyword(memo.text, keyword)
  )
})

const countText = computed(() => {
  if (importantSearchActive.value) {
    return `${filteredImportantMemos.value.length} / ${sortedImportantMemos.value.length} 条`
  }
  return `${sortedImportantMemos.value.length} 条`
})

const clearImportantSearch = () => {
  importantSearchKeyword.value = ''
}

const handleDeleteImportant = async (memo) => {
  try {
    await ElMessageBox.confirm('确定删除这条重要记录吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await memoStore.removeImportantMemo(memo.id)
    ElMessage.success('已删除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>

<template>
  <section
    class="memo-section memo-section--important"
    :class="{ 'is-header-only': collapsed }"
  >
    <MemoSectionHead
      title="重要记录"
      :count-text="countText"
      :expanded="expanded"
      accent-title
      @toggle="emit('toggle')"
    />

    <Transition name="section-panel">
      <div v-show="!bodyHidden" class="section-body-wrap">
        <div class="important-body">
          <div class="important-main">
            <div v-if="loading" class="section-state">加载中…</div>
            <div v-else-if="sortedImportantMemos.length === 0" class="section-state">
              <p>暂无重要记录</p>
              <small>可在「录入」中选择「重要备忘」添加</small>
            </div>
            <div
              v-else-if="filteredImportantMemos.length === 0"
              class="section-state"
            >
              <p>没有匹配「{{ importantSearchKeyword.trim() }}」的记录</p>
              <small>
                试试更短的关键词，或
                <button type="button" class="link-btn" @click="clearImportantSearch">
                  清空搜索
                </button>
              </small>
            </div>
            <div v-else class="card-scroll">
              <ImportantMemoCard
                v-for="memo in filteredImportantMemos"
                :key="memo.id"
                :memo="memo"
                @delete="handleDeleteImportant(memo)"
              />
            </div>
          </div>

          <aside class="important-filter">
            <h4 class="filter-title">搜索</h4>
            <div class="filter-search">
              <input
                v-model="importantSearchKeyword"
                type="search"
                class="filter-input"
                placeholder="输入内容关键词"
                autocomplete="off"
              />
              <button
                v-if="importantSearchActive"
                type="button"
                class="btn-clear-search"
                @click="clearImportantSearch"
              >
                清空
              </button>
            </div>
            <p class="filter-count">
              显示 {{ filteredImportantMemos.length }} /
              {{ sortedImportantMemos.length }} 条
            </p>
          </aside>
        </div>
      </div>
    </Transition>
  </section>
</template>

<style scoped lang="less">
.important-body {
  flex: 1 1 0%;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr minmax(140px, 180px);
  gap: 12px;
  align-items: stretch;
  overflow: visible;
}

.important-main {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.important-filter {
  margin: 2px 2px 2px 0;
  padding: 16px 14px;
  background: var(--box-mi-bai);
  border: 1px solid var(--border);
  border-radius: 10px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 1px 4px rgba(195, 39, 43, 0.04);
}

.filter-search {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-input {
  width: 100%;
  padding: 9px 12px;
  border: 2px solid var(--border);
  border-radius: 8px;
  background: #fff;
  font-size: 13px;
  color: var(--text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.15s;

  &:focus {
    border-color: rgba(195, 39, 43, 0.35);
  }

  &::placeholder {
    color: var(--text-muted);
  }
}

.btn-clear-search {
  align-self: flex-start;
  padding: 5px 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #fff;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  transition: background 0.15s, color 0.15s;

  &:hover {
    background: var(--bg-active);
    color: var(--text);
  }
}

@media (max-width: 720px) {
  .important-body {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr auto;
    gap: 10px;
  }

  .important-filter {
    margin: 0;
    padding: 14px 16px;
    border-radius: 10px;
  }

  .filter-search {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
  }
}
</style>
