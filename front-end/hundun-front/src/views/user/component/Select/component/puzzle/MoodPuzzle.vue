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

const MOOD_EMOJI = ['😫', '😔', '😐', '😊', '😄']
const MOOD_LABELS = ['很差', '差', '一般', '好', '很好']
const MOOD_COLORS = [
  'rgba(195, 39, 43, 0.20)',
  'rgba(195, 39, 43, 0.35)',
  'rgba(195, 39, 43, 0.50)',
  'rgba(195, 39, 43, 0.70)',
  'rgba(195, 39, 43, 0.90)'
]

const avgLabel = computed(() => {
  if (!props.summary || props.summary.totalDays === 0) return '暂无数据'
  const avg = props.summary.average
  if (avg >= 3.5) return '很好 ↑'
  if (avg >= 2.5) return '好 ↑'
  if (avg >= 1.5) return '一般 →'
  if (avg >= 0.5) return '差 ↓'
  return '很差 ↓'
})

const avgEmoji = computed(() => {
  if (!props.summary || props.summary.totalDays === 0) return '😶'
  const avg = props.summary.average
  if (avg >= 3.5) return '😄'
  if (avg >= 2.5) return '😊'
  if (avg >= 1.5) return '😐'
  if (avg >= 0.5) return '😔'
  return '😫'
})

const distributionList = computed(() => {
  if (!props.summary || !props.summary.distribution) return []
  return Object.entries(props.summary.distribution).map(([key, count]) => ({
    mood: Number(key),
    count,
    emoji: MOOD_EMOJI[Number(key)],
    label: MOOD_LABELS[Number(key)],
    color: MOOD_COLORS[Number(key)]
  }))
})

const latestEntries = computed(() => {
  if (!props.summary || !props.summary.entries) return []
  return props.expanded
    ? props.summary.entries
    : props.summary.entries.slice(0, 3)
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr + 'T00:00:00')
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<template>
  <div
    class="mood-puzzle"
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
        <span class="empty-emoji">😶</span>
        <span>暂无心情记录</span>
        <small>去「录入」添加每日心情</small>
      </div>
    </template>

    <template v-else>
      <div class="puzzle-header">
        <span class="puzzle-icon">😊</span>
        <span class="puzzle-title">心情</span>
        <span class="puzzle-count">{{ summary.totalDays }}天</span>
      </div>

      <div class="avg-section">
        <span class="avg-value">{{ summary.average }}</span>
        <span class="avg-emoji">{{ avgEmoji }}</span>
        <span class="avg-label">{{ avgLabel }}</span>
      </div>

      <div class="dist-section">
        <div class="dist-bar">
          <div
            v-for="d in distributionList"
            :key="d.mood"
            class="dist-segment"
            :style="{
              flex: d.count,
              background: d.count > 0 ? d.color : 'transparent',
              minWidth: d.count > 0 ? '4px' : '0'
            }"
          />
        </div>
        <div class="dist-labels">
          <span v-for="d in distributionList" :key="'l'+d.mood" class="dist-label">
            {{ d.emoji }} {{ d.count > 0 ? d.count : '' }}
          </span>
        </div>
      </div>

      <div v-if="latestEntries.length" class="latest-section" :class="{ expanded: expanded }">
        <div v-for="entry in latestEntries" :key="entry.date" class="latest-item">
          <span class="latest-date">{{ formatDate(entry.date) }}</span>
          <span class="latest-mood">{{ MOOD_EMOJI[entry.mood] }}</span>
          <span class="latest-note" :title="entry.note">{{ entry.note || '—' }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="less">
.mood-puzzle {
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

/* 关闭按钮 */
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
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
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
  align-items: baseline;
  gap: 8px;
  padding: 8px 0 4px;
}
.avg-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--accent);
  line-height: 1;
  letter-spacing: -1px;
  transition: font-size 0.35s ease;
}
.avg-emoji {
  font-size: 22px;
  transition: font-size 0.35s ease;
}
.avg-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}

/* 展开时放大核心数值 */
.expanded .avg-value { font-size: 40px; }
.expanded .avg-emoji { font-size: 28px; }

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

.latest-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--bg-soft);
  min-height: 0;
  transition: all 0.35s ease;

  &.expanded {
    flex: 1;
    overflow-y: auto;
    margin-top: 0;
    gap: 8px;
    padding-top: 14px;

    &::-webkit-scrollbar {
      width: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: var(--card-border);
      border-radius: 2px;
    }

    .latest-item {
      font-size: 14px;
      gap: 10px;
      padding: 4px 0;
      animation: itemFadeIn 0.35s ease both;
    }

    .latest-date {
      width: 40px;
      font-size: 12px;
    }

    .latest-mood {
      font-size: 18px;
    }
  }
}

.latest-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}
.latest-date {
  color: var(--text-muted);
  flex-shrink: 0;
  width: 32px;
}
.latest-mood { flex-shrink: 0; }
.latest-note {
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

@keyframes itemFadeIn {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
