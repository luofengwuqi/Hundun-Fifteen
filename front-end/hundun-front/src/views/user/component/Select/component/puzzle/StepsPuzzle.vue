<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  summary: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

const hoveredBar = ref(null)

const chartEntries = computed(() => {
  if (!props.summary || !props.summary.entries) return []
  return [...props.summary.entries].sort(
    (a, b) => new Date(a.date) - new Date(b.date)
  )
})

const maxSteps = computed(() => {
  return Math.max(...chartEntries.value.map(e => e.steps || 0), 1)
})

const barCount = computed(() => chartEntries.value.length)

const formatShortDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr + 'T00:00:00')
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const formatSteps = (n) => {
  if (n == null) return '0'
  return n.toLocaleString()
}

const showBars = computed(() => chartEntries.value.length > 0)

const labelInterval = computed(() => {
  const count = barCount.value
  if (count <= 10) return 1
  if (count <= 20) return 2
  return Math.ceil(count / 10)
})
</script>

<template>
  <div class="steps-puzzle">
    <div v-if="loading" class="puzzle-loading">
      <span class="loading-spinner" />
      <span>加载中...</span>
    </div>

    <template v-else-if="!summary || summary.totalDays === 0">
      <div class="puzzle-empty">
        <span class="empty-emoji">🚶</span>
        <span>暂无外出记录</span>
        <small>去「录入」添加每日外出</small>
      </div>
    </template>

    <template v-else>
      <div class="puzzle-header">
        <span class="puzzle-icon">🚶</span>
        <span class="puzzle-title">步数</span>
        <span class="puzzle-count">{{ summary.totalDays }}天</span>
      </div>

      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-value">{{ formatSteps(summary.totalSteps) }}</span>
          <span class="stat-label">总步数</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-value">{{ formatSteps(Math.round(summary.avgDailySteps)) }}</span>
          <span class="stat-label">日均步数</span>
        </div>
      </div>

      <div class="chart-area">
        <div v-if="!showBars" class="chart-empty">区间内无步数数据</div>

        <div v-else class="bar-chart" @mouseleave="hoveredBar = null">
          <div
            v-for="(entry, idx) in chartEntries"
            :key="entry.date"
            class="bar-column"
            @mouseenter="hoveredBar = entry"
          >
            <div
              class="bar-fill"
              :class="{ 'is-hovered': hoveredBar === entry }"
              :style="{ height: (entry.steps / maxSteps) * 100 + '%' }"
            />
          </div>
        </div>

        <div v-if="showBars" class="x-labels">
          <span
            v-for="(entry, idx) in chartEntries"
            :key="'x'+entry.date"
            class="x-label"
            :class="{ 'is-visible': idx % labelInterval === 0 }"
          >{{ formatShortDate(entry.date) }}</span>
        </div>

        <transition name="tooltip-fade">
          <div
            v-if="hoveredBar"
            class="bar-tooltip"
          >
            <p class="tip-date">{{ hoveredBar.date }}</p>
            <p class="tip-steps">👣 {{ formatSteps(hoveredBar.steps) }} 步</p>
            <p v-if="hoveredBar.location" class="tip-loc">📍 {{ hoveredBar.location }}</p>
            <p v-if="hoveredBar.transportMode" class="tip-trans">🚌 {{ hoveredBar.transportMode }}</p>
          </div>
        </transition>
      </div>
    </template>
  </div>
</template>

<style scoped lang="less">
.steps-puzzle {
  --card-bg: #ffffff;
  --card-border: #eee8d8;
  --text: #333333;
  --text-muted: #999999;
  --accent: #c3272b;
  --accent-light: rgba(195, 39, 43, 0.30);
  --bg-soft: #fff8e8;

  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 20px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 12px;
}

.puzzle-loading,
.puzzle-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 13px;
}
.empty-emoji { font-size: 28px; }
.puzzle-empty small {
  font-size: 11px;
  color: var(--text-muted);
}
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--bg-soft);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.puzzle-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.puzzle-icon { font-size: 18px; }
.puzzle-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}
.puzzle-count {
  margin-left: auto;
  font-size: 11px;
  color: var(--text-muted);
  background: var(--bg-soft);
  padding: 2px 8px;
  border-radius: 8px;
}

.stats-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 6px 0;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent);
  line-height: 1;
}
.stat-label {
  font-size: 11px;
  color: var(--text-muted);
}
.stat-divider {
  width: 1px;
  height: 28px;
  background: var(--card-border);
}

.chart-area {
  flex: 1;
  min-height: 0;
  position: relative;
  display: flex;
  flex-direction: column;
}
.chart-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-muted);
}

.bar-chart {
  flex: 1;
  display: flex;
  align-items: flex-end;
  gap: 3px;
  min-height: 100px;
  padding: 0 2px;
  overflow-x: auto;
  overflow-y: visible;

  &::-webkit-scrollbar { height: 4px; }
  &::-webkit-scrollbar-thumb { background: var(--line); border-radius: 2px; }
}
.bar-column {
  flex: 1;
  min-width: 8px;
  max-width: 40px;
  display: flex;
  align-items: flex-end;
  cursor: pointer;
  position: relative;
}
.bar-fill {
  width: 100%;
  background: linear-gradient(to top, var(--accent), rgba(195, 39, 43, 0.60));
  border-radius: 3px 3px 0 0;
  transition: opacity 0.2s, transform 0.2s;
  min-height: 2px;
  opacity: 0.75;
}
.bar-fill:hover,
.bar-fill.is-hovered {
  opacity: 1;
  transform: scaleX(1.08);
}

.x-labels {
  display: flex;
  gap: 3px;
  margin-top: 6px;
  padding: 0 2px;
}
.x-label {
  flex: 1;
  text-align: center;
  font-size: 9px;
  color: var(--text-muted);
  white-space: nowrap;
  min-width: 0;
  &:not(.is-visible) {
    visibility: hidden;
  }
}

.bar-tooltip {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%) translateY(-100%);
  margin-top: -8px;
  background: #333;
  color: #fff;
  font-size: 12px;
  line-height: 1.5;
  padding: 8px 12px;
  border-radius: 8px;
  white-space: nowrap;
  z-index: 10;
  pointer-events: none;

  p { margin: 0; }
  .tip-date { font-weight: 600; margin-bottom: 2px; }
  .tip-steps,
  .tip-loc,
  .tip-trans { opacity: 0.85; }
}

.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: opacity 0.15s ease;
}
.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
}
</style>
