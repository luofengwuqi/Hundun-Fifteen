<script setup>
import { computed } from 'vue'

const props = defineProps({
  summary: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  expanded: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle-expand'])

const hasData = computed(() => props.summary && props.summary.totalDays > 0)

const handleCardClick = () => {
  if (!props.expanded && hasData.value) {
    emit('toggle-expand')
  }
}

const handleClose = () => {
  emit('toggle-expand')
}

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

const chartEntries = computed(() => {
  if (!props.summary || !props.summary.entries) return []
  const sorted = [...props.summary.entries].sort(
    (a, b) => new Date(a.date) - new Date(b.date)
  )
  // 展开时展示全部，收起时展示近 14 条
  return props.expanded ? sorted : sorted.slice(-14)
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
  <div
    class="sleep-puzzle"
    :class="{ expanded: expanded, clickable: !expanded && hasData }"
    @click="handleCardClick"
  >
    <!-- 展开态关闭按钮 -->
    <button
      v-if="expanded"
      class="close-btn"
      title="收起"
      @click.stop="handleClose"
    >
      ✕
    </button>

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

      <div v-if="chartEntries.length" class="chart-section" :class="{ expanded: expanded }">
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

  position: relative;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 20px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 12px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;

  &.clickable {
    cursor: pointer;

    &:hover {
      border-color: var(--accent);
      box-shadow: 0 4px 20px rgba(195, 39, 43, 0.12);
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
    }
  }

  &.expanded {
    padding: 24px 28px;
    gap: 16px;
  }
}

.close-btn {
  position: absolute;
  top: -14px;
  right: 14px;
  z-index: 10;
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--card-border);
  border-radius: 50%;
  background: var(--card-bg);
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  animation: closeBtnIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    background: var(--accent);
    color: #fff;
    border-color: var(--accent);
    transform: rotate(90deg);
  }
}

@keyframes closeBtnIn {
  from { opacity: 0; transform: scale(0.5); }
  to { opacity: 1; transform: scale(1); }
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
  transition: font-size 0.35s ease;
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
.quality-emoji { font-size: 16px; transition: font-size 0.35s ease; }
.quality-label {
  font-size: 12px;
  color: var(--text);
  font-weight: 500;
}

.expanded .avg-value { font-size: 30px; }
.expanded .quality-emoji { font-size: 20px; }

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
  transition: height 0.35s ease;
}
.expanded .dist-bar { height: 14px; }
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
  transition: font-size 0.35s ease;
}
.expanded .dist-label { font-size: 12px; }

.chart-section {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--bg-soft);
  transition: all 0.35s ease;
  display: flex;
  flex-direction: column;
  min-height: 0;

  &.expanded {
    flex: 1;
    padding-top: 14px;

    .mini-chart {
      flex: 1;
      min-height: 120px;
    }

    .mini-label {
      font-size: 10px;
    }
  }
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
  transition: height 0.35s ease;
  overflow-x: auto;

  &::-webkit-scrollbar { height: 4px; }
  &::-webkit-scrollbar-thumb {
    background: var(--card-border);
    border-radius: 2px;
  }
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
