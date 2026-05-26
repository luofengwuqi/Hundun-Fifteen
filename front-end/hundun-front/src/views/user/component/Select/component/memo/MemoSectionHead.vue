<script setup>
defineProps({
  title: { type: String, required: true },
  countText: { type: String, required: true },
  expanded: { type: Boolean, default: false },
  accentTitle: { type: Boolean, default: false }
})

const emit = defineEmits(['toggle'])

const onKeydown = (e) => {
  if (e.key === 'Enter' || e.key === ' ') {
    e.preventDefault()
    emit('toggle')
  }
}
</script>

<template>
  <header
    class="section-head section-head--clickable"
    role="button"
    tabindex="0"
    title="点击展开/收起"
    @click="emit('toggle')"
    @keydown="onKeydown"
  >
    <div class="section-head-leading">
      <h3 class="section-title" :class="{ 'is-accent': accentTitle }">{{ title }}</h3>
      <span
        class="expand-chevron"
        :class="{ 'is-flipped': expanded }"
        aria-hidden="true"
      >▾</span>
    </div>
    <span class="section-count">{{ countText }}</span>
  </header>
</template>

<style scoped lang="less">
.section-title.is-accent {
  color: var(--accent);
}
</style>
