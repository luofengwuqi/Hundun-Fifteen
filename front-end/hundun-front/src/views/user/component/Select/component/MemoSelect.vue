<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user.js'
import FunctionDisabled from './FunctionDisabled.vue'
import MemoContent from './MemoContent.vue'

const userStore = useUserStore()
const enabled = computed(() => userStore.userModelStatus?.memo === 1)
</script>

<template>
  <FunctionDisabled v-if="!enabled" module-name="备忘" />
  <MemoContent v-else />
</template>

<style lang="less">
@import './memo/memo-shared.less';
</style>
