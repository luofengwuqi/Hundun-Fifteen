<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useMemoStore } from '@/stores/memo.js'

const memoStore = useMemoStore()
const loading = ref(false)
const updatingCommonId = ref(null)
const todoFilter = ref('all')
const importantSearchKeyword = ref('')

/** null | 'todo' | 'important' — 点击标题切换半屏聚焦 */
const focusSection = ref(null)

const toggleTodoFocus = () => {
  focusSection.value = focusSection.value === 'todo' ? null : 'todo'
}

const toggleImportantFocus = () => {
  focusSection.value = focusSection.value === 'important' ? null : 'important'
}

const TODO_FILTER_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: 'pending', label: '未完成' },
  { value: 'done', label: '已完成' }
]

const STATUS_MAP = {
  0: { label: '通知关闭', class: 'off' },
  1: { label: '通知开启', class: 'on' },
  2: { label: '已完成', class: 'done' }
}

const sortedCommonMemos = computed(() => {
  return [...memoStore.commonMemos].sort((a, b) => {
    const ta = new Date(a.beginTime || 0).getTime()
    const tb = new Date(b.beginTime || 0).getTime()
    return tb - ta
  })
})

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

/** 内容模糊匹配：忽略大小写，子串包含即命中 */
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

const clearImportantSearch = () => {
  importantSearchKeyword.value = ''
}

const formatDateTime = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const formatRange = (begin, end) => {
  if (!begin && !end) return '—'
  return `${formatDateTime(begin)} → ${formatDateTime(end)}`
}

const getStatusMeta = (status) => STATUS_MAP[status] ?? { label: '未知', class: 'off' }

/** 与后端 @JsonFormat 一致：yyyy-MM-dd HH:mm */
const toApiDateTime = (value) => {
  if (!value) return value
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const nowApiDateTime = () => toApiDateTime(new Date())

const buildCommonDto = (memo, patch = {}) => ({
  id: memo.id,
  beginTime: toApiDateTime(memo.beginTime),
  endTime: toApiDateTime(memo.endTime),
  text: memo.text,
  status: memo.status,
  email: memo.email ?? undefined,
  ...patch
})

const updateCommonMemo = async (memo, patch) => {
  updatingCommonId.value = memo.id
  try {
    await memoStore.editCommonMemo(memo.id, buildCommonDto(memo, patch), todoFilter.value)
  } finally {
    updatingCommonId.value = null
  }
}

const handleComplete = async (memo) => {
  if (memo.status === 2) return
  try {
    await updateCommonMemo(memo, {
      endTime: nowApiDateTime(),
      status: 2
    })
    ElMessage.success('已标记完成')
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const handleToggleNotify = async (memo) => {
  if (memo.status === 2) {
    ElMessage.info('已完成项无法修改通知')
    return
  }
  const nextStatus = memo.status === 1 ? 0 : 1
  try {
    await updateCommonMemo(memo, { status: nextStatus })
    ElMessage.success(nextStatus === 1 ? '已开启邮件通知' : '已关闭邮件通知')
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const loadCommonMemos = async () => {
  try {
    await memoStore.getCommonMemos(todoFilter.value)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载 To Do 失败')
    throw e
  }
}

const loadMemos = async () => {
  loading.value = true
  try {
    await Promise.all([loadCommonMemos(), memoStore.getImportantMemos()])
  } catch (e) {
    console.error(e)
    if (!memoStore.commonMemos.length) {
      ElMessage.error('加载备忘录失败')
    }
  } finally {
    loading.value = false
  }
}

watch(todoFilter, () => {
  loadCommonMemos()
})

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

onMounted(() => {
  loadMemos()
})
</script>

<template>
  <div
    class="memo-select select-panel"
    :class="{
      'is-todo-focused': focusSection === 'todo',
      'is-important-focused': focusSection === 'important'
    }"
  >
    <!-- 上栏：To Do List -->
    <section
      class="memo-section memo-section--todo"
      :class="{ 'is-header-only': focusSection === 'important' }"
    >
      <header
        class="section-head section-head--clickable"
        role="button"
        tabindex="0"
        title="点击展开/收起"
        @click="toggleTodoFocus"
        @keydown.enter.prevent="toggleTodoFocus"
        @keydown.space.prevent="toggleTodoFocus"
      >
        <div class="section-head-leading">
          <h3 class="section-title">To Do List</h3>
          <span
            class="expand-chevron"
            :class="{ 'is-flipped': focusSection === 'todo' }"
            aria-hidden="true"
          >▾</span>
        </div>
        <span class="section-count">{{ sortedCommonMemos.length }} 条</span>
      </header>

      <Transition name="section-panel">
        <div v-show="focusSection !== 'important'" class="section-body-wrap">
      <div class="todo-body">
        <div class="todo-main">
          <div v-if="loading" class="section-state">加载中…</div>
          <div v-else-if="sortedCommonMemos.length === 0" class="section-state">
            <p>
              {{
                todoFilter === 'done'
                  ? '暂无已完成记录'
                  : todoFilter === 'pending'
                    ? '暂无未完成待办'
                    : '暂无待办'
              }}
            </p>
            <small>可在「录入」中添加备忘录</small>
          </div>
          <div v-else class="card-scroll">
        <article
          v-for="memo in sortedCommonMemos"
          :key="memo.id"
          class="memo-card"
          :class="{ 'is-done': memo.status === 2 }"
        >
          <div class="card-top">
            <span class="status-tag" :class="getStatusMeta(memo.status).class">
              {{ getStatusMeta(memo.status).label }}
            </span>
            <button
              type="button"
              class="btn-delete"
              title="删除"
              @click="handleDeleteCommon(memo)"
            >
              ×
            </button>
          </div>
          <p class="card-text">{{ memo.text }}</p>
          <p class="card-meta">{{ formatRange(memo.beginTime, memo.endTime) }}</p>
          <div class="card-actions">
            <button
              v-if="memo.status !== 2"
              type="button"
              class="btn-action btn-action--primary"
              :disabled="updatingCommonId === memo.id"
              @click="handleComplete(memo)"
            >
              完成
            </button>
            <button
              v-if="memo.status !== 2"
              type="button"
              class="btn-action"
              :disabled="updatingCommonId === memo.id"
              @click="handleToggleNotify(memo)"
            >
              {{ memo.status === 1 ? '关闭邮件通知' : '开启邮件通知' }}
            </button>
            <span v-else class="done-tip">已于 {{ formatDateTime(memo.endTime) }} 完成</span>
          </div>
        </article>
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

    <!-- 下栏：重要记录 -->
    <section
      class="memo-section memo-section--important"
      :class="{ 'is-header-only': focusSection === 'todo' }"
    >
      <header
        class="section-head section-head--clickable"
        role="button"
        tabindex="0"
        title="点击展开/收起"
        @click="toggleImportantFocus"
        @keydown.enter.prevent="toggleImportantFocus"
        @keydown.space.prevent="toggleImportantFocus"
      >
        <div class="section-head-leading">
          <h3 class="section-title">重要记录</h3>
          <span
            class="expand-chevron"
            :class="{ 'is-flipped': focusSection === 'important' }"
            aria-hidden="true"
          >▾</span>
        </div>
        <span class="section-count">
          <template v-if="importantSearchActive">
            {{ filteredImportantMemos.length }} / {{ sortedImportantMemos.length }} 条
          </template>
          <template v-else>
            {{ sortedImportantMemos.length }} 条
          </template>
        </span>
      </header>

      <Transition name="section-panel">
        <div v-show="focusSection !== 'todo'" class="section-body-wrap">
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
            <small>试试更短的关键词，或<button
              type="button"
              class="link-btn"
              @click="clearImportantSearch"
            >清空搜索</button></small>
          </div>
          <div v-else class="card-scroll">
            <article
              v-for="memo in filteredImportantMemos"
              :key="memo.id"
              class="memo-card memo-card--accent"
            >
              <div class="card-top">
                <span class="accent-mark">★</span>
                <button
                  type="button"
                  class="btn-delete"
                  title="删除"
                  @click="handleDeleteImportant(memo)"
                >
                  ×
                </button>
              </div>
              <p class="card-text">{{ memo.text }}</p>
              <p class="card-meta">记录于 {{ formatDateTime(memo.createdAt) }}</p>
            </article>
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
  </div>
</template>

<style scoped lang="less">
.memo-select {
  --bg: #ffffff;
  --bg-soft: #fff8e8;
  --box-mi-bai: #fff8e8;
  --bg-active: #ffefd5;
  --text: #333333;
  --text-muted: #999999;
  --accent: #c3272b;
  --border: #eee8d8;
  --line: #e8e0d0;
  --panel-duration: 0.38s;
  --panel-ease: cubic-bezier(0.4, 0, 0.2, 1);

  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg);
  overflow: hidden;
  color: var(--text);
  font-size: 14px;
  line-height: 1.5;
}

.memo-section {
  flex: 1 1 0%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 28px 16px;
  transition:
    flex-grow var(--panel-duration) var(--panel-ease),
    flex-shrink var(--panel-duration) var(--panel-ease),
    flex-basis var(--panel-duration) var(--panel-ease),
    padding var(--panel-duration) var(--panel-ease);

  & + & {
    border-top: 2px solid var(--bg-soft);
  }

  &.is-header-only {
    flex: 0 0 auto;
    padding-bottom: 12px;

    .section-head {
      margin-bottom: 0;
      padding-bottom: 10px;
    }
  }
}

.memo-select.is-todo-focused {
  .memo-section--todo {
    flex: 1 1 0%;
  }

  .memo-section--important {
    flex: 0 0 auto;
  }
}

.memo-select.is-important-focused {
  .memo-section--todo {
    flex: 0 0 auto;
  }

  .memo-section--important {
    flex: 1 1 0%;
  }
}

.memo-section--todo {
  flex: 1.15 1 0%;
}

.section-body-wrap {
  flex: 1 1 0%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-panel-enter-active,
.section-panel-leave-active {
  transition:
    opacity calc(var(--panel-duration) * 0.85) var(--panel-ease),
    transform var(--panel-duration) var(--panel-ease),
    max-height var(--panel-duration) var(--panel-ease);
  overflow: hidden;
}

.section-panel-enter-from,
.section-panel-leave-to {
  opacity: 0;
  transform: translateY(-10px);
  max-height: 0;
}

.section-panel-enter-to,
.section-panel-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 2000px;
}

.todo-body,
.important-body {
  flex: 1 1 0%;
  min-height: 0;
}

.todo-body {
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

  .filter-title {
    margin: 0;
    padding-top: 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--text);
  }
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

.section-head {
  flex-shrink: 0;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
  transition:
    margin-bottom var(--panel-duration) var(--panel-ease),
    padding-bottom var(--panel-duration) var(--panel-ease),
    border-color var(--panel-duration) var(--panel-ease);
}

.section-head--clickable {
  cursor: pointer;
  user-select: none;
  border-radius: 8px;
  margin-left: -6px;
  margin-right: -6px;
  padding-left: 6px;
  padding-right: 6px;
  transition:
    background 0.2s ease,
    margin-bottom var(--panel-duration) var(--panel-ease),
    padding-bottom var(--panel-duration) var(--panel-ease);

  &:hover {
    background: rgba(255, 248, 232, 0.85);

    .section-title {
      color: var(--accent);
    }
  }

  &:focus-visible {
    outline: 2px solid rgba(195, 39, 43, 0.35);
    outline-offset: 2px;
  }
}

.section-head-leading {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.expand-chevron {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1;
  transition: transform var(--panel-duration) var(--panel-ease), color 0.2s ease;

  &.is-flipped {
    transform: rotate(180deg);
    color: var(--accent);
  }
}

.memo-section.is-header-only .section-head--clickable:hover {
  background: var(--bg-soft);
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: 0.3px;
}

.memo-section--important {
  flex: 1 1 0%;
}

.memo-section--important .section-title {
  color: var(--accent);
}

.important-body {
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

  .filter-title {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--text);
  }
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

.filter-count {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
}

.link-btn {
  padding: 0;
  border: none;
  background: none;
  color: var(--accent);
  font-size: inherit;
  cursor: pointer;
  text-decoration: underline;

  &:hover {
    opacity: 0.85;
  }
}

.section-count {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
}

.section-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--text-muted);
  text-align: center;

  small {
    font-size: 13px;
  }
}

.card-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
  align-content: start;
  padding-bottom: 8px;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--line);
    border-radius: 3px;
  }
}

.todo-main .memo-card {
  background: var(--box-mi-bai);
}

.memo-card {
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 14px 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;

  &:hover {
    border-color: var(--line);
    box-shadow: 0 2px 10px rgba(195, 39, 43, 0.06);
  }

  &.is-done {
    opacity: 0.72;

    .card-text {
      text-decoration: line-through;
      color: var(--text-muted);
    }
  }
}

.memo-card--accent {
  background: #fff;
  border-color: rgba(195, 39, 43, 0.12);
  box-shadow: 0 1px 6px rgba(195, 39, 43, 0.05);
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;

  &.on {
    background: var(--bg-active);
    color: var(--text);
  }

  &.off {
    background: #f5f5f5;
    color: var(--text-muted);
  }

  &.done {
    background: rgba(195, 39, 43, 0.08);
    color: var(--accent);
  }
}

.accent-mark {
  font-size: 14px;
  color: var(--accent);
  line-height: 1;
}

.btn-delete {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-muted);
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;

  &:hover {
    background: rgba(195, 39, 43, 0.08);
    color: var(--accent);
  }
}

.card-text {
  margin: 0;
  font-size: 14px;
  color: var(--text);
  word-break: break-word;
  line-height: 1.55;
}

.card-meta {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.4;
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding-top: 10px;
  border-top: 1px dashed var(--border);
}

.btn-action {
  padding: 6px 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #fff;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;

  &:hover:not(:disabled) {
    border-color: var(--line);
    color: var(--text);
  }

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &--primary {
    background: var(--bg-active);
    border-color: rgba(195, 39, 43, 0.15);
    color: var(--text);
    font-weight: 500;

    &:hover:not(:disabled) {
      border-color: rgba(195, 39, 43, 0.25);
      color: var(--accent);
    }
  }
}

.done-tip {
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 720px) {
  .memo-section {
    padding: 16px 18px 12px;
  }

  .todo-body {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr auto;
    gap: 10px;
  }

  .todo-main {
    border-right: none;
    border-bottom: none;
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

  .card-scroll {
    grid-template-columns: 1fr;
  }
}
</style>
