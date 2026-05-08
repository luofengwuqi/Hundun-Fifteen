import request from '@/utils/request.js'

export const loginByUsernameAndPassword = async (username , password)=> {
  return await request.post(
     '/user/login/username-password',
     {
      username: username,
      password: password
    }
  )
}

export const loginByPhoneAndPassword = async (phone , password)=>{
  return await  request.post('/user/login/phone-password',
    {
    phone,
    password,
  })
}

export const changeStatus = async (username)=>{
  // console.log(useUserStore().token)
  return await request.put(`/user/status?username=${username}`)
}

export const logoutAPI = async ()=>{
  return await request.post('/user/login/logout')
}

export const getInviteCode = async ()=>{
  return await request.get('/admin/invite-code')
}

export const deleteSingleUser = async (id)=> {
  return await request.delete(`/user/${id}`)
}

export const addAdmin = async (admin)=>{
  return await request.post('/admin',admin)
}

export const addUser = async (user)=>{
  return await request.post('/user/register',user)
}

export const sendEmailCode = async (email, skipDefaultErrorHandling = false) =>{
  return await request.post('/user/login/email-code/send',{
    email:email
  }, {
    skipDefaultErrorHandling
  })
}

export const loginByEmailAndCode = async (email,code)=>{
  return await request.post('/user/login/email-code',{
    email,
    code
  })
}

export const loginByEmailAndPassword = async (email,password)=>{
  return await request.post('/user/login/email-password',{
    email,
    password
  })
}

export const updateAvatarAPI = async (formData) => {
  return await request.put('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const getUserInfoAPI = async (id) => {
  return await request.get('/user/info',{
    params: {
      id
    }
  })
}



export const updateNicknameAPI = async (huh,password) => {
  return await request.put('/user/nickname', {
    huh,
    password
  })
}

export const updateEmailAPI = async (huh,password) => {
  return await request.put('/user/email', {
    huh,
    password
  })
}

export const updatePhoneAPI = async (huh,password) => {
  return await request.put('/user/phone', {
    huh,
    password
  })
}

export const updatePasswordAPI = async (code,password) => {
  return await request.put('/user/password', {
    code,
    password
  })
}

// 统一更新用户信息接口
export const updateUserAPI = async (dto) => {
  return await request.put('/user', dto)
}

//userModelStatus相关代码

/**
 * 获取用户模块状态
 */
export const getUserModelStatus = async () => {
  return await request.get('/user/model-status')
}

/**
 * 更新用户模块状态
 * @param {Object} dto - 用户模块状态数据
 * @param {number} dto.health - 健康模块 0禁用 1启用
 * @param {number} dto.finance - 财务模块 0禁用 1启用
 * @param {number} dto.knowledge - 知识模块 0禁用 1启用
 * @param {number} dto.memo - 备忘模块 0禁用 1启用
 * @param {number} dto.userId - 用户ID
 */
export const updateUserModelStatus = async (dto) => {
  return await request.put('/user/model-status', dto)
}

/**
 * 删除用户模块状态（警告：此操作有一定危险性）
 * @param {Object} userModelStatus - 用户模块状态对象
 * @param {number} userModelStatus.userId - 用户ID
 */
export const deleteUserModelStatus = async (userModelStatus) => {
  return await request.delete('/user/model-status', {
    data: userModelStatus
  })
}
