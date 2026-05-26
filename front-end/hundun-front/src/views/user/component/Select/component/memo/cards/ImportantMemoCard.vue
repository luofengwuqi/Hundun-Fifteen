<script setup>
import { formatDateTime } from '../composables/useMemoDatetime.js'

defineProps({
  memo: { type: Object, required: true }
})

const emit = defineEmits(['delete'])
</script>

<template>
  <article class="memo-card memo-card--accent">
    <div class="card-top">
      <span class="accent-mark">★</span>
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
    <p class="card-meta">记录于 {{ formatDateTime(memo.createdAt) }}</p>
  </article>
</template>
