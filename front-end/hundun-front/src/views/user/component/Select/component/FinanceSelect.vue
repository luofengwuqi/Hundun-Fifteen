<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useFinanceStore } from '@/stores/finance.js'

const financeStore = useFinanceStore()

const categoryFilter = ref('all')
const searchKeyword = ref('')
const timelineFilter = ref('all')
const dateRangeStart = ref('')
const dateRangeEnd = ref('')

const CATEGORY_ALL = 'all'

const timelineFilterOptions = [
  { value: 'all', label: '全部' },
  { value: '7d', label: '近 7 天' },
  { value: '30d', label: '近 30 天' },
  { value: 'month', label: '本月' },
  { value: 'custom', label: '自定义' }
]

const parseDate = (dateStr) => new Date(`${dateStr}T00:00:00`)

const filteredTimeline = computed(() => {
  const list = financeStore.timeline
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  return list.filter((day) => {
    const d = parseDate(day.date)
    if (Number.isNaN(d.getTime())) return false

    switch (timelineFilter.value) {
      case '7d': {
        const start = new Date(today)
        start.setDate(start.getDate() - 6)
        return d >= start && d <= today
      }
      case '30d': {
        const start = new Date(today)
        start.setDate(start.getDate() - 29)
        return d >= start && d <= today
      }
      case 'month':
        return (
          d.getFullYear() === today.getFullYear() &&
          d.getMonth() === today.getMonth()
        )
      case 'custom': {
        if (dateRangeStart.value && d < parseDate(dateRangeStart.value)) return false
        if (dateRangeEnd.value && d > parseDate(dateRangeEnd.value)) return false
        return true
      }
      default:
        return true
    }
  })
})

watch([filteredTimeline, timelineFilter], () => {
  const expanded = financeStore.expandedDate
  if (!expanded) return
  const visible = filteredTimeline.value.some((d) => d.date === expanded)
  if (!visible) financeStore.collapseDetail()
})
const PRESET_CATEGORIES = ['饮食', '购物']

const categoryOptions = computed(() => {
  const fromDetail = [
    ...new Set(financeStore.detailList.map((item) => item.title).filter(Boolean))
  ]
  const titles = fromDetail.length ? fromDetail : PRESET_CATEGORIES
  return [
    { value: CATEGORY_ALL, label: '全部' },
    ...titles.map((title) => ({ value: title, label: title }))
  ]
})

const filteredSections = computed(() => {
  if (!financeStore.expandedDate) return []

  let sections = financeStore.detailList.map((section) => ({
    title: section.title,
    dataMap: { ...section.dataMap }
  }))

  if (categoryFilter.value !== CATEGORY_ALL) {
    sections = sections.filter((s) => s.title === categoryFilter.value)
  }

  const keyword = searchKeyword.value.trim()
  if (keyword) {
    sections = sections
      .map((s) => ({
        ...s,
        dataMap: Object.fromEntries(
          Object.entries(s.dataMap).filter(([name]) => name.includes(keyword))
        )
      }))
      .filter((s) => Object.keys(s.dataMap).length > 0)
  }

  return sections
})

/** 后端金额以「分」存储（×100），展示为元 */
const formatMoney = (amount) => {
  const yuan = Number(amount) / 100
  if (Number.isNaN(yuan)) return '0'
  return yuan % 1 === 0 ? String(yuan) : yuan.toFixed(2)
}

const formatAxisDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(`${dateStr}T00:00:00`)
  return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日`
}

const toggleDate = async (date) => {
  if (financeStore.expandedDate === date) {
    financeStore.collapseDetail()
    return
  }

  try {
    await financeStore.fetchDetail(date)
  } catch (e) {
    console.error(e)
    ElMessage.error('加载当日明细失败')
  }
}

const initFinance = async () => {
  try {
    await financeStore.fetchTimeline()
  } catch (e) {
    console.error(e)
    ElMessage.error('加载财务时间线失败')
  }
}

onMounted(() => {
  initFinance()
})
</script>

<template>
  <div class="finance-select select-panel">
    <!-- 左：时间轴树 -->
    <section class="axis-panel">
      <header class="axis-head">
        <h3 class="axis-title">财务记录</h3>
      </header>

      <div v-if="financeStore.loadingTimeline" class="axis-state">加载中…</div>

      <div
        v-else-if="financeStore.timeline.length === 0"
        class="axis-state"
      >
        <p>暂无消费记录</p>
        <small>去「录入」添加饮食或购物</small>
      </div>

      <div
        v-else-if="filteredTimeline.length === 0"
        class="axis-state"
      >
        <p>该时间段暂无记录</p>
        <small>试试调整右侧时间筛选</small>
      </div>

      <div v-else class="axis-scroll">
        <ul class="axis-tree">
          <li
            v-for="day in filteredTimeline"
            :key="day.date"
            class="axis-day"
            :class="{ open: financeStore.expandedDate === day.date }"
          >
            <div class="day-row">
              <span class="axis-dot" aria-hidden="true" />
              <button
                type="button"
                class="day-trigger"
                @click="toggleDate(day.date)"
              >
                <span class="day-label">{{ formatAxisDate(day.date) }}</span>
                <span class="day-total">共消费 {{ formatMoney(day.totalPrice) }} 元</span>
              </button>
            </div>

            <div v-if="financeStore.expandedDate === day.date" class="day-detail">
              <p v-if="financeStore.loadingDetail" class="detail-hint">加载明细…</p>
              <p v-else-if="filteredSections.length === 0" class="detail-hint">
                无符合筛选的记录
              </p>

              <template v-else>
                <ul class="detail-tree">
                  <li
                    v-for="section in filteredSections"
                    :key="section.title"
                    class="tree-branch"
                  >
                    <p class="branch-head">{{ section.title }}</p>
                    <ul
                      v-if="Object.keys(section.dataMap).length"
                      class="branch-children"
                    >
                      <li
                        v-for="(price, name) in section.dataMap"
                        :key="name"
                        class="tree-leaf"
                      >
                        <div class="leaf-content">
                          <span class="leaf-label">{{ name }}</span>
                          <span class="leaf-amount">{{ formatMoney(price) }} 元</span>
                        </div>
                      </li>
                    </ul>
                  </li>
                </ul>
              </template>
            </div>
          </li>
        </ul>
      </div>
    </section>

    <!-- 右：筛选器 -->
    <aside class="filter-panel">
      <header class="filter-head">
        <h4 class="filter-title">筛选</h4>
        <p v-if="financeStore.expandedDate" class="filter-hint">
          {{ formatAxisDate(financeStore.expandedDate) }}
        </p>
        <p v-else class="filter-hint muted">点击左侧日期展开明细</p>
      </header>

      <div class="filter-group">
        <span class="filter-label">时间线</span>
        <div class="filter-chips">
          <button
            v-for="opt in timelineFilterOptions"
            :key="opt.value"
            type="button"
            class="chip"
            :class="{ active: timelineFilter === opt.value }"
            @click="timelineFilter = opt.value"
          >
            {{ opt.label }}
          </button>
        </div>
        <div v-if="timelineFilter === 'custom'" class="date-range">
          <label class="date-field">
            <span>起</span>
            <input v-model="dateRangeStart" type="date" class="filter-input" />
          </label>
          <label class="date-field">
            <span>止</span>
            <input v-model="dateRangeEnd" type="date" class="filter-input" />
          </label>
        </div>
        <p class="filter-count">
          显示 {{ filteredTimeline.length }} / {{ financeStore.timeline.length }} 天
        </p>
      </div>

      <div class="filter-group">
        <span class="filter-label">分类</span>
        <div class="filter-chips">
          <button
            v-for="opt in categoryOptions"
            :key="opt.value"
            type="button"
            class="chip"
            :class="{ active: categoryFilter === opt.value }"
            @click="categoryFilter = opt.value"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>

      <div class="filter-group">
        <span class="filter-label">项目</span>
        <input
          v-model="searchKeyword"
          type="search"
          class="filter-input"
          placeholder="名称关键词"
          autocomplete="off"
        />
      </div>
    </aside>
  </div>
</template>

<style scoped lang="less">
.finance-select {
  --bg: #ffffff;
  --bg-soft: #fff8e8;
  --bg-active: #ffefd5;
  --text: #333333;
  --text-muted: #999999;
  --accent: #c3272b;
  --line: #e8e0d0;
  --border: #eee8d8;

  width: 100%;
  height: 100%;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr minmax(200px, 240px);
  background: var(--bg);
  border-radius: 0;
  box-shadow: none;
  overflow: hidden;
  color: var(--text);
  font-size: 14px;
  line-height: 1.5;
}

.axis-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  border-right: 1px solid var(--border);
}

.axis-head {
  flex-shrink: 0;
  padding: 22px 28px 16px;
  border-bottom: 2px solid var(--bg-soft);
}

.axis-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: 0.5px;
}

.axis-scroll {
  flex: 1;
  overflow: auto;
  padding: 20px 28px 28px;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--line);
    border-radius: 3px;
  }
}

.axis-state {
  padding: 56px 28px;
  text-align: center;
  color: var(--text-muted);

  small {
    display: block;
    margin-top: 8px;
    font-size: 13px;
  }
}

/* 时间轴主线 */
.axis-tree {
  list-style: none;
  margin: 0;
  padding: 0 0 0 6px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 11px;
    top: 8px;
    bottom: 8px;
    width: 2px;
    background: var(--line);
    border-radius: 1px;
  }
}

.axis-day {
  position: relative;
  padding-bottom: 4px;

  &:last-child {
    padding-bottom: 0;
  }

  &.open .axis-dot {
    background: var(--accent);
    box-shadow: 0 0 0 4px rgba(195, 39, 43, 0.12);
  }

  &.open .day-trigger {
    background: var(--bg-soft);
  }

  &.open .day-label {
    color: var(--text);
    font-weight: 600;
  }

  &.open .day-total {
    color: var(--accent);
    font-weight: 600;
  }
}

.day-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 6px 0;
}

.axis-dot {
  flex-shrink: 0;
  width: 12px;
  height: 12px;
  margin-top: 6px;
  border-radius: 50%;
  background: var(--bg-active);
  border: 2px solid var(--line);
  position: relative;
  z-index: 1;
  transition: background 0.2s, box-shadow 0.2s;
}

.day-trigger {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 6px 20px;
  padding: 10px 14px;
  margin: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  border-radius: 10px;
  transition: background 0.2s ease;

  &:hover {
    background: var(--bg-soft);
  }
}

.day-label {
  font-size: 15px;
  color: var(--text-muted);
  transition: color 0.2s;
}

.day-total {
  font-size: 14px;
  color: var(--text-muted);
  white-space: nowrap;
}

/* 展开明细 · 树状 */
.day-detail {
  --tree-line: #e0d8c8;
  --tree-indent: 26px;
  --tree-spine: 1px;
  --price-width: 5.5em;

  margin: 8px 0 20px 18px;
  padding: 2px 16px 10px 0;
}

.detail-hint {
  margin: 0 0 10px;
  padding-left: var(--tree-indent);
  font-size: 13px;
  color: var(--text-muted);
}

.detail-tree {
  list-style: none;
  margin: 0;
  padding: 0 12px 0 var(--tree-spine);
  position: relative;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  border-left: var(--tree-spine) solid var(--tree-line);
}

.tree-branch {
  position: relative;

  & + & {
    margin-top: 10px;
  }
}

/* 分类：从主干横出的节点 */
.branch-head {
  position: relative;
  margin: 0;
  padding: 10px 0 8px var(--tree-indent);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--text);

  &::before {
    content: '';
    position: absolute;
    left: calc(-1 * var(--tree-spine));
    top: 50%;
    width: calc(var(--tree-indent) - 4px);
    height: var(--tree-spine);
    background: var(--tree-line);
    transform: translateY(-50%);
  }
}

/* 子项列表：左侧竖干 + 每行圆角横枝 */
.branch-children {
  list-style: none;
  margin: 0 0 4px calc(var(--tree-indent) - 10px);
  padding: 2px 0 6px calc(var(--tree-indent) + 4px);
  position: relative;
  border-left: var(--tree-spine) solid var(--tree-line);
}

.tree-leaf {
  position: relative;

  & + & {
    margin-top: 2px;
  }

  /* 圆角 L 形横枝 */
  &::before {
    content: '';
    position: absolute;
    left: calc(-1 * (var(--tree-indent) + 4px));
    top: 50%;
    width: calc(var(--tree-indent) - 2px);
    height: 11px;
    border-left: var(--tree-spine) solid var(--tree-line);
    border-bottom: var(--tree-spine) solid var(--tree-line);
    border-bottom-left-radius: 8px;
    transform: translateY(-6px);
    pointer-events: none;
  }

  /* 末项下方不延长竖线 */
  &:last-child::after {
    content: '';
    position: absolute;
    left: calc(-1 * (var(--tree-indent) + 5px));
    top: calc(50% + 4px);
    bottom: -6px;
    width: 3px;
    background: var(--bg);
  }
}

.leaf-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) var(--price-width);
  align-items: center;
  column-gap: 24px;
  min-height: 36px;
  padding: 2px 0;
}

.leaf-label {
  font-size: 14px;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.leaf-amount {
  text-align: right;
  font-size: 13px;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

/* 右侧筛选 */
.filter-panel {
  min-height: 0;
  overflow-y: auto;
  padding: 22px 20px;
  background: var(--bg-soft);
  display: flex;
  flex-direction: column;
  gap: 22px;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--line);
    border-radius: 3px;
  }
}

.filter-head {
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border);
}

.filter-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.filter-hint {
  margin: 0;
  font-size: 12px;
  color: var(--text);
  line-height: 1.5;

  &.muted {
    color: var(--text-muted);
  }
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.filter-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-muted);
  letter-spacing: 0.5px;
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
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
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
    box-shadow: 0 2px 8px rgba(195, 39, 43, 0.08);
  }
}

.date-range {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
}

.date-field {
  display: flex;
  flex-direction: column;
  gap: 4px;

  span {
    font-size: 11px;
    color: var(--text-muted);
  }
}

.filter-count {
  margin: 4px 0 0;
  font-size: 11px;
  color: var(--text-muted);
}

.filter-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--bg);
  font-size: 13px;
  color: var(--text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus {
    border-color: rgba(195, 39, 43, 0.25);
    box-shadow: 0 0 0 3px rgba(195, 39, 43, 0.06);
  }

  &::placeholder {
    color: var(--text-muted);
  }
}

@media (max-width: 768px) {
  .finance-select {
    grid-template-columns: 1fr;
  }

  .axis-panel {
    border-right: none;
    border-bottom: 1px solid var(--border);
  }

  .filter-panel {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 16px;
  }

  .filter-chips {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .chip {
    flex: 0 0 auto;
  }
}
</style>
