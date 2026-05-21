import request from '@/utils/request.js'

/** 财务时间线：每日总支出 */
export const getFinanceTimelineAPI = () => {
  return request.get('/finance')
}

/** 指定日期的分类明细 */
export const getFinanceDetailAPI = (date) => {
  return request.get(`/finance/${date}`)
}
