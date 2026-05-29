<script setup>
import { ref, watch, onMounted } from 'vue'
import { useHealthStore } from '@/stores/health.js'
import MoodPuzzle from './puzzle/MoodPuzzle.vue'
import SleepPuzzle from './puzzle/SleepPuzzle.vue'
import StepsPuzzle from './puzzle/StepsPuzzle.vue'

const healthStore = useHealthStore()

const timeFilter = ref('all')
const dateRangeStart = ref('')
const dateRangeEnd = ref('')

// 当前展开的拼图组件：null | 'mood' | 'sleep' | 'steps'
const expandedPuzzle = ref(null)

const handleToggleExpand = (type) => {
  expandedPuzzle.value = expandedPuzzle.value === type ? null : type
}

const filterOptions = [
  { value: 'all', label: '全部' },
  { value: '7d', label: '近 7 天' },
  { value: '30d', label: '近 30 天' },
  { value: 'month', label: '本月' },
  { value: 'custom', label: '自定义' }
]

const computeRange = () => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const fmt = (d) => {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }

  switch (timeFilter.value) {
    case 'all': return { start: undefined, end: undefined }
    case '7d': {
      const start = new Date(today)
      start.setDate(start.getDate() - 6)
      return { start: fmt(start), end: fmt(today) }
    }
    case '30d': {
      const start = new Date(today)
      start.setDate(start.getDate() - 29)
      return { start: fmt(start), end: fmt(today) }
    }
    case 'month': {
      const start = new Date(today.getFullYear(), today.getMonth(), 1)
      return { start: fmt(start), end: fmt(today) }
    }
    case 'custom': {
      return {
        start: dateRangeStart.value || undefined,
        end: dateRangeEnd.value || undefined
      }
    }
  }
}

const loadData = async () => {
  const { start, end } = computeRange()
  await healthStore.fetchAll(start, end)
}

watch(timeFilter, () => {
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="health-select">
    <header class="health-header">
      <div class="header-left">
        <span class="header-icon">🏥</span>
        <h3 class="header-title">健康总览</h3>
      </div>
      <div class="header-filters">
        <button
          v-for="opt in filterOptions"
          :key="opt.value"
          type="button"
          class="filter-chip"
          :class="{ active: timeFilter === opt.value }"
          @click="timeFilter = opt.value"
        >
          {{ opt.label }}
        </button>
      </div>
      <div v-if="timeFilter === 'custom'" class="custom-range">
        <label class="date-field">
          <span>从</span>
          <input v-model="dateRangeStart" type="date" class="date-input" />
        </label>
        <label class="date-field">
          <span>到</span>
          <input v-model="dateRangeEnd" type="date" class="date-input" />
        </label>
        <button type="button" class="query-btn" @click="loadData">查询</button>
      </div>
    </header>

    <div class="puzzle-grid" :class="{ 'has-expanded': !!expandedPuzzle }">
      <!-- 心情拼图 -->
      <div
        v-show="!expandedPuzzle || expandedPuzzle === 'mood'"
        class="puzzle-cell puzzle-cell-collapsible"
        :class="{ 'puzzle-cell-expanded': expandedPuzzle === 'mood' }"
      >
        <MoodPuzzle
          :summary="healthStore.moodSummary"
          :loading="healthStore.loadingMood"
          :expanded="expandedPuzzle === 'mood'"
          @toggle-expand="handleToggleExpand('mood')"
        />
      </div>

      <!-- 睡眠拼图 -->
      <div
        v-show="!expandedPuzzle || expandedPuzzle === 'sleep'"
        class="puzzle-cell puzzle-cell-collapsible"
        :class="{ 'puzzle-cell-expanded': expandedPuzzle === 'sleep' }"
      >
        <SleepPuzzle
          :summary="healthStore.sleepSummary"
          :loading="healthStore.loadingSleep"
          :expanded="expandedPuzzle === 'sleep'"
          @toggle-expand="handleToggleExpand('sleep')"
        />
      </div>

      <!-- 步数拼图 -->
      <div
        v-show="!expandedPuzzle || expandedPuzzle === 'steps'"
        class="puzzle-cell puzzle-cell-full puzzle-cell-collapsible"
        :class="{ 'puzzle-cell-expanded': expandedPuzzle === 'steps' }"
      >
        <StepsPuzzle
          :summary="healthStore.stepsSummary"
          :loading="healthStore.loadingSteps"
          :expanded="expandedPuzzle === 'steps'"
          @toggle-expand="handleToggleExpand('steps')"
        />
      </div>

      <!-- 饮食占位 -->
      <div v-show="!expandedPuzzle" class="puzzle-cell puzzle-cell-full puzzle-cell-collapsible">
        <div class="food-placeholder">
          <span class="food-icon">🍜</span>
          <span class="food-label">饮食拼图</span>
          <span class="food-status">筹备中...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.health-select {
  --bg: #ffffff;
  --bg-soft: #fff8e8;
  --bg-active: #ffefd5;
  --text: #333333;
  --text-muted: #999999;
  --accent: #c3272b;
  --border: #eee8d8;
  --line: #e8e0d0;

  width: 100%;
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 22px 28px;
  box-sizing: border-box;
  background: var(--bg);
  color: var(--text);
  font-size: 14px;
  line-height: 1.5;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--line);
    border-radius: 3px;
  }
}

.health-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--bg-soft);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-icon { font-size: 22px; }
.header-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: 0.5px;
}

.header-filters {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.filter-chip {
  padding: 6px 12px;
  border: 2px solid transparent;
  border-radius: 8px;
  background: var(--bg);
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  text-align: center;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(195, 39, 43, 0.04);

  &:hover {
    color: var(--text);
    border-color: var(--border);
  }

  &.active {
    background: var(--bg-active);
    border-color: rgba(195, 39, 43, 0.15);
    color: var(--text);
    font-weight: 600;
    box-shadow: 0 2px 6px rgba(195, 39, 43, 0.08);
  }
}

.custom-range {
  display: flex;
  align-items: center;
  gap: 8px;
}
.date-field {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}
.date-input {
  padding: 5px 8px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--bg);
  font-size: 12px;
  color: var(--text);
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;

  &:focus {
    border-color: rgba(195, 39, 43, 0.25);
    box-shadow: 0 0 0 2px rgba(195, 39, 43, 0.06);
  }
}

.query-btn {
  padding: 5px 14px;
  border: none;
  border-radius: 6px;
  background: var(--accent);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #d64a4f;
  }
}

.puzzle-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  align-content: start;
  transition: grid-template-columns 0.4s cubic-bezier(0.4, 0, 0.2, 1),
              gap 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &.has-expanded {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr;
    align-content: stretch;
    gap: 0;
  }
}

.puzzle-cell {
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.puzzle-cell-collapsible {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.puzzle-cell-expanded {
  grid-column: 1 / -1;
  min-height: 0;
  animation: cellExpandIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.puzzle-cell-full {
  grid-column: 1 / -1;
}

@keyframes cellExpandIn {
  from {
    opacity: 0.8;
    transform: scale(0.98);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.food-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 24px;
  background: var(--bg);
  border: 1px dashed var(--border);
  border-radius: 12px;
  color: var(--text-muted);
  font-size: 14px;
}
.food-icon { font-size: 20px; }
.food-label { font-weight: 500; color: var(--text); }
.food-status { font-size: 12px; color: var(--text-muted); }
</style>
