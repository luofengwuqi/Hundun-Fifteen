<script setup>
import { computed, ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  summary: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  expanded: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle-expand'])

/* ---------- 展开 / 收起 ---------- */
const hasData = computed(() => props.summary && props.summary.totalDays > 0)

const handleCardClick = () => {
  if (!props.expanded && hasData.value) {
    emit('toggle-expand')
  }
}

const handleClose = () => {
  emit('toggle-expand')
}

/* ---------- 数据 ---------- */
const chartEntries = computed(() => {
  if (!props.summary || !props.summary.entries) return []
  return [...props.summary.entries].sort(
    (a, b) => new Date(a.date) - new Date(b.date)
  )
})

const formatShortDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr + 'T00:00:00')
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const formatSteps = (n) => {
  if (n == null) return '0'
  return n.toLocaleString()
}

const showChart = computed(() => chartEntries.value.length > 0)

/* ---------- ECharts ---------- */
const chartDomRef = ref(null)
let chartInstance = null
let resizeObserver = null

/* ---------- 工具：等待 DOM 元素拥有有效尺寸 ---------- */
const waitContainerReady = (dom, maxRetries = 10, interval = 50) => {
  return new Promise((resolve) => {
    if (dom && dom.clientWidth > 0 && dom.clientHeight > 0) {
      resolve(true)
      return
    }
    let retries = 0
    const timer = setInterval(() => {
      retries++
      if (dom && dom.clientWidth > 0 && dom.clientHeight > 0) {
        clearInterval(timer)
        resolve(true)
        return
      }
      if (retries >= maxRetries) {
        clearInterval(timer)
        resolve(false)
      }
    }, interval)
  })
}

const buildOption = () => {
  const entries = chartEntries.value
  if (entries.length === 0) return {}

  const dates = entries.map(e => e.date)
  const steps = entries.map(e => e.steps || 0)

  return {
    grid: {
      left: 50,
      right: 10,
      top: 8,
      bottom: 8
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        show: true,
        fontSize: 10,
        color: '#999',
        formatter: (val) => formatShortDate(val),
        interval: props.expanded ? 0 : 'auto'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        lineStyle: { color: '#f0ebe0', type: 'dashed' }
      },
      axisLabel: {
        fontSize: 10,
        color: '#999',
        formatter: (val) => formatSteps(val)
      }
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#333',
      borderColor: '#333',
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (params) => {
        if (!params || params.length === 0) return ''
        const p = params[0]
        const entry = entries[p.dataIndex]
        const lines = [
          `<strong>${entry.date}</strong>`,
          `👣 ${formatSteps(entry.steps)} 步`
        ]
        if (entry.location) lines.push(`📍 ${entry.location}`)
        if (entry.transportMode) lines.push(`🚌 ${entry.transportMode}`)
        return lines.join('<br/>')
      }
    },
    series: [
      {
        type: 'line',
        data: steps,
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        showSymbol: entries.length <= 30,
        lineStyle: {
          color: '#c3272b',
          width: 2
        },
        itemStyle: {
          color: '#c3272b',
          borderColor: '#fff',
          borderWidth: 1.5
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(195, 39, 43, 0.15)' },
            { offset: 1, color: 'rgba(195, 39, 43, 0.01)' }
          ])
        }
      }
    ]
  }
}

/** 销毁图表实例并清理 ResizeObserver */
const disposeChart = () => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
}

/** 初始化 / 重新初始化图表 */
const initChart = async () => {
  disposeChart()
  await nextTick()
  const dom = chartDomRef.value
  if (!dom) return

  // 等容器拥有有效尺寸后再初始化 ECharts
  const ready = await waitContainerReady(dom)
  if (!ready) {
    // 如果超时仍无尺寸，先用一个兜底尺寸初始化，ResizeObserver 后续会纠正
    console.warn('[StepsPuzzle] chart container size is 0, init with fallback')
  }

  chartInstance = echarts.init(dom)
  chartInstance.setOption(buildOption())

  // 使用 ResizeObserver 自动跟随容器尺寸变化
  if (!resizeObserver) {
    resizeObserver = new ResizeObserver(() => {
      if (chartInstance) chartInstance.resize()
    })
  }
  resizeObserver.observe(dom)
}

/** 仅更新图表配置（不重新初始化） */
const updateChart = () => {
  if (!chartInstance) return
  chartInstance.setOption(buildOption(), true)
}

/** 执行 resize（ResizeObserver 已覆盖大部分场景，此方法作为补充） */
const resizeChart = () => {
  if (chartInstance) chartInstance.resize()
}

// 当数据就绪且 DOM 出现时，初始化图表
watch(showChart, async (val) => {
  if (val) {
    await nextTick()
    if (!chartInstance && chartDomRef.value) {
      initChart()
    } else {
      updateChart()
    }
  } else {
    disposeChart()
  }
})

// 数据变化 → 更新图表（图表已存在时）
watch(chartEntries, () => {
  if (chartInstance) updateChart()
}, { deep: true })

// 展开态变化 → 更新配置，并在浏览器完成布局后 resize
watch(() => props.expanded, async () => {
  // 先让 DOM 更新
  await nextTick()
  // 再等一帧让浏览器完成布局重排
  await new Promise((resolve) => requestAnimationFrame(resolve))

  if (chartInstance) {
    updateChart()
    resizeChart()
  } else if (chartDomRef.value && showChart.value) {
    // 图表尚未初始化（极少情况），重新初始化
    initChart()
  }
})

onMounted(() => {
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  disposeChart()
})
</script>

<template>
  <div
    class="steps-puzzle"
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

      <!-- ECharts 折线图 -->
      <div class="chart-area" :class="{ expanded: expanded }">
        <div v-if="!showChart" class="chart-empty">区间内无步数数据</div>
        <div v-else ref="chartDomRef" class="chart-dom" />
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
  --bg-soft: #fff8e8;

  position: relative;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 18px;
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
    gap: 12px;
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

.stats-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 2px 0;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
  line-height: 1;
  transition: font-size 0.35s ease;
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

.expanded .stat-value { font-size: 26px; }

// ============ 图表 ============
.chart-area {
  flex: 1;
  min-height: 0;
  position: relative;
  display: flex;
  flex-direction: column;
  transition: all 0.35s ease;
}
.chart-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-muted);
}

.chart-dom {
  flex: 1;
  min-height: 80px;
  width: 100%;
}
</style>
