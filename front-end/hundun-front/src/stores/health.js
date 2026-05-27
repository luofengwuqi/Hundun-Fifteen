import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getMoodSummaryAPI, getSleepSummaryAPI, getStepsSummaryAPI } from '@/api/health.js'

export const useHealthStore = defineStore('health', () => {
  const moodSummary = ref(null)
  const sleepSummary = ref(null)
  const stepsSummary = ref(null)

  const loadingMood = ref(false)
  const loadingSleep = ref(false)
  const loadingSteps = ref(false)

  const fetchMood = async (start, end) => {
    loadingMood.value = true
    try {
      const res = await getMoodSummaryAPI(start, end)
      moodSummary.value = res.data || null
      return res
    } finally {
      loadingMood.value = false
    }
  }

  const fetchSleep = async (start, end) => {
    loadingSleep.value = true
    try {
      const res = await getSleepSummaryAPI(start, end)
      sleepSummary.value = res.data || null
      return res
    } finally {
      loadingSleep.value = false
    }
  }

  const fetchSteps = async (start, end) => {
    loadingSteps.value = true
    try {
      const res = await getStepsSummaryAPI(start, end)
      stepsSummary.value = res.data || null
      return res
    } finally {
      loadingSteps.value = false
    }
  }

  const fetchAll = async (start, end) => {
    await Promise.all([
      fetchMood(start, end).catch(() => {}),
      fetchSleep(start, end).catch(() => {}),
      fetchSteps(start, end).catch(() => {})
    ])
  }

  return {
    moodSummary, sleepSummary, stepsSummary,
    loadingMood, loadingSleep, loadingSteps,
    fetchMood, fetchSleep, fetchSteps, fetchAll
  }
})
