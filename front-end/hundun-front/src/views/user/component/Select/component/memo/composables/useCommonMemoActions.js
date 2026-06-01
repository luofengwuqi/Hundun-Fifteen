import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useMemoStore } from '@/stores/memo.js'
import { nowApiDateTime, toApiDateTime } from './useMemoDatetime.js'

export function useCommonMemoActions(todoFilter) {
  const memoStore = useMemoStore()
  const updatingCommonId = ref(null)

  const buildCommonDto = (memo, patch = {}) => ({
    id: memo.id,
    beginTime: toApiDateTime(memo.beginTime),
    endTime: toApiDateTime(memo.endTime),
    text: memo.text,
    status: memo.status,
    email: memo.email ?? undefined,
    remindBeforeHours: memo.remindBeforeHours ?? 12,
    ...patch
  })

  const updateCommonMemo = async (memo, patch) => {
    updatingCommonId.value = memo.id
    try {
      await memoStore.editCommonMemo(
        memo.id,
        buildCommonDto(memo, patch),
        todoFilter.value
      )
    } finally {
      updatingCommonId.value = null
    }
  }

  const handleComplete = async (memo) => {
    if (memo.status === 2) return
    try {
      await updateCommonMemo(memo, {
        endTime: nowApiDateTime(),
        status: 2
      })
      ElMessage.success('已标记完成')
    } catch (e) {
      console.error(e)
      ElMessage.error('操作失败')
    }
  }

  const handleToggleNotify = async (memo) => {
    if (memo.status === 2) {
      ElMessage.info('已完成项无法修改通知')
      return
    }
    const nextStatus = memo.status === 1 ? 0 : 1
    try {
      await updateCommonMemo(memo, { status: nextStatus })
      ElMessage.success(nextStatus === 1 ? '已开启邮件通知' : '已关闭邮件通知')
    } catch (e) {
      console.error(e)
      ElMessage.error('操作失败')
    }
  }

  return {
    updatingCommonId,
    handleComplete,
    handleToggleNotify
  }
}
