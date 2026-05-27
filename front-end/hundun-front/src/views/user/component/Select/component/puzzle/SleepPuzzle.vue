<script setup>
import { computed } from 'vue'

const props = defineProps({
  summary: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})

const QUALITY_EMOJI = ['😴', '😐', '😊']
const QUALITY_LABELS = ['差', '一般', '好']
const QUALITY_COLORS = [
  'rgba(195, 39, 43, 0.25)',
  'rgba(195, 39, 43, 0.50)',
  'rgba(195, 39, 43, 0.80)'
]

const formatDuration = (min) => {
  if (min == null || min === 0) return '—'
  const h = Math.floor(min / 60)
  const m = Math.round(min % 60)
  return `${h}h ${m}m`
}

const avgQualityLabel = computed(() => {
  if (!props.summary || props.summary.totalDays === 0) return '暂无数据'
  const q = props.summary.avgQuality
  if (q >= 1.5) return '好'
  if (q >= 0.5) return '一般'
  return '差'
})

const avgQualityEmoji = computed(() => {
  if (!props.summary || props.summary.totalDays === 0) return '😶'
  const q = props.summary.avgQuality
  if (q >= 1.5) return '😊'
  if (q >= 0.5) return '😐'
  return '😴'
})

const distributionList = computed(() => {
  if (!props.summary || !props.summary.distribution) return []
  return Object.entries(props.summary.distribution).map(([key, count]) => ({
    quality: Number(key),
    count,
    emoji: QUALITY_EMOJI[Number(key)],
    label: QUALITY_LABELS[Number(key)],
    color: QUALITY_COLORS[Number(key)]
  }))
})

const totalDistCount = computed(() => {
  return distributionList.value.reduce((s, d) => s + d.count, 0)
})

const chartEntries = computed(() => {
  if (!props.summary || !props.summary.entries) return []
  const sorted = [...props.summary.entries].sort(
    (a, b) => new Date(a.date) - new Date(b.date)
  )
  return sorted.slice(-14)
})

const maxDuration = computed(() => {
  return Math.max(...chartEntries.value.map(e => e.durationMin || 0), 1)
})

const formatShortDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr + 'T00:00:00')
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<template>
  <div class="sleep-puzzle">
    <div v-if="loading" class="puzzle-loading">
      <span class="loading-spinner" />
      <span>加载中...</span>
    </div>

    <template v-else-if="!summary || summary.totalDays === 0">
      <div class="puzzle-empty">
        <span class="empty-emoji">😴</span>
        <span>暂无睡眠记录</span>
        <small>去「录入」添加每日睡眠</small>
      </div>
    </template>

    <template v-else>
      <div class="puzzle-header">
        <span class="puzzle-icon">😴</span>
        <span class="puzzle-title">睡眠</span>
        <span class="puzzle-count">{{ summary.totalDays }}天</span>
      </div>

      <div class="avg-section">
        <div class="avg-duration">
          <span class="avg-value">{{ formatDuration(summary.avgDuration) }}</span>
          <span class="avg-label">平均时长</span>
        </div>
        <div class="avg-quality-display">
          <span class="quality-emoji">{{ avgQualityEmoji }}</span>
          <span class="quality-label">{{ avgQualityLabel }}</span>
        </div>
      </div>

      <div class="dist-section">
        <div class="dist-bar">
          <div
            v-for="d in distributionList"
            :key="d.quality"
            class="dist-segment"
            :style="{
              flex: d.count,
              background: d.count > 0 ? d.color : 'transparent',
              minWidth: d.count > 0 ? '4px' : '0'
            }"
          />
        </div>
        <div class="dist-labels">
          <span v-for="d in distributionList" :key="'l'+d.quality" class="dist-label">
            {{ d.emoji }} {{ d.count > 0 ? d.count : '' }}
          </span>
        </div>
      </div>

      <div v-if="chartEntries.length" class="chart-section">
        <span class="chart-title">时长趋势</span>
        <div class="mini-chart">
          <div
            v-for="entry in chartEntries"
            :key="entry.date"
            class="mini-bar-wrap"
            :title="`${entry.date} - ${formatDuration(entry.durationMin)}`"
          >
            <div
              class="mini-bar"
              :style="{ height: (entry.durationMin / maxDuration) * 100 + '%' }"
            />
            <span class="mini-label">{{ formatShortDate(entry.date) }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="less">
.sleep-puzzle {
  --card-bg: #ffffff;
  --card-border: #eee8d8;
  --text: #333333;
  --text-muted: #999999;
  --accent: #c3272b;
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

.avg-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 4px;
}
.avg-duration {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.avg-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--accent);
  line-height: 1;
  letter-spacing: -0.5px;
}
.avg-label {
  font-size: 11px;
  color: var(--text-muted);
}
.avg-quality-display {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--bg-soft);
  border-radius: 8px;
}
.quality-emoji { font-size: 16px; }
.quality-label {
  font-size: 12px;
  color: var(--text);
  font-weight: 500;
}

.dist-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.dist-bar {
  display: flex;
  height: 10px;
  border-radius: 5px;
  overflow: hidden;
  background: #f5f5f5;
}
.dist-segment {
  transition: flex 0.3s ease;
  border-radius: 5px;
}
.dist-labels {
  display: flex;
  justify-content: space-between;
}
.dist-label {
  font-size: 10px;
  color: var(--text-muted);
  line-height: 1.2;
}

.chart-section {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--bg-soft);
}
.chart-title {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
  display: block;
  margin-bottom: 8px;
}
.mini-chart {
  display: flex;
  align-items: flex-end;
  height: 48px;
  gap: 3px;
}
.mini-bar-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
  min-width: 0;
  cursor: default;
}
.mini-bar {
  width: 100%;
  max-width: 20px;
  background: var(--accent);
  border-radius: 2px 2px 0 0;
  opacity: 0.65;
  transition: opacity 0.2s;
  min-height: 2px;
}
.mini-bar-wrap:hover .mini-bar {
  opacity: 1;
}
.mini-label {
  font-size: 8px;
  color: var(--text-muted);
  margin-top: 2px;
  white-space: nowrap;
}
</style>
