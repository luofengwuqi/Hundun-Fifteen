import request from '@/utils/request.js'

export const savePaymentAPI = (dtoList) => {
  return request.post('/payment', dtoList)
}
