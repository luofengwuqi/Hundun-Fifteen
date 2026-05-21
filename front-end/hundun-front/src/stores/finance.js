import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getFinanceTimelineAPI, getFinanceDetailAPI } from '@/api/finance.js'

export const useFinanceStore = defineStore('finance', () => {
  const timeline = ref([])
  const detailList = ref([])
  const expandedDate = ref('')
  const loadingTimeline = ref(false)
  const loadingDetail = ref(false)

  const fetchTimeline = async () => {
    loadingTimeline.value = true
    try {
      const res = await getFinanceTimelineAPI()
      const list = res.data || []
      timeline.value = [...list].sort(
        (a, b) => new Date(b.date) - new Date(a.date)
      )
      return res
    } finally {
      loadingTimeline.value = false
    }
  }

  const fetchDetail = async (date) => {
    if (!date) return
    loadingDetail.value = true
    try {
      const res = await getFinanceDetailAPI(date)
      detailList.value = res.data || []
      expandedDate.value = date
      return res
    } finally {
      loadingDetail.value = false
    }
  }

  const collapseDetail = () => {
    expandedDate.value = ''
    detailList.value = []
  }

  return {
    timeline,
    detailList,
    expandedDate,
    loadingTimeline,
    loadingDetail,
    fetchTimeline,
    fetchDetail,
    collapseDetail
  }
})
