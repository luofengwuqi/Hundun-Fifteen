import request from '@/utils/request.js'

const insertFoodAPI = async (dto)=>{
  return await request.post('/health',{
    dto
  })

}

const insertFoodMoneyAPI = async (dto)=>{
    return await request.post('/finance',{
      dto
  })
}
