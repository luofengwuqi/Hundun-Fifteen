import request from '@/utils/request.js'

export const getMoodSummaryAPI = (start, end) => {
  return request.get('/health/mood', { params: { start, end } })
}

export const getSleepSummaryAPI = (start, end) => {
  return request.get('/health/sleep', { params: { start, end } })
}

export const getStepsSummaryAPI = (start, end) => {
  return request.get('/health/place/steps', { params: { start, end } })
}
