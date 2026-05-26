<script setup>
import { getStatusMeta } from '../composables/useMemoStatus.js'
import { formatDateTime, formatRange } from '../composables/useMemoDatetime.js'

defineProps({
  memo: { type: Object, required: true },
  updating: { type: Boolean, default: false }
})

const emit = defineEmits(['delete', 'complete', 'toggle-notify'])
</script>

<template>
  <article
    class="memo-card memo-card--mi-bai"
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
        @click="emit('delete')"
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
        :disabled="updating"
        @click="emit('complete')"
      >
        完成
      </button>
      <button
        v-if="memo.status !== 2"
        type="button"
        class="btn-action"
        :disabled="updating"
        @click="emit('toggle-notify')"
      >
        {{ memo.status === 1 ? '关闭邮件通知' : '开启邮件通知' }}
      </button>
      <span v-else class="done-tip">已于 {{ formatDateTime(memo.endTime) }} 完成</span>
    </div>
  </article>
</template>
