import request from '@/utils/request.js'

export const insertFoodAPI = async (dto)=>{
  return await request.post('/health',dto)

}

export const insertFoodMoneyAPI = async (dto)=>{
    return await request.post('food/finance',dto)
}


export const deleteFoodMoneyAPI = async (id)=>{
  return await request.delete(`food/finance/${id}`)
}

export const updateFoodMoneyAPI = async (dto)=>{
  return await request.put(`food/finance`,dto)
}
