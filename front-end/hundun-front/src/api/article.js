import request from '@/utils/request'

// 获取文章列表
export const getArticleListAPI = () => {
  return request({
    url: '/article',
    method: 'GET'
  })
}

// 获取单篇文章
export const getArticleByIdAPI = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'GET'
  })
}
export const submitHelpArticle = (title, text, creator) => {
  return request.post('/article', {
    title,
    text,
    creator,
    belonging: 1
  })
}

export const submitArticleAPI = (data) => {
  return request.post('/article', data)
}

export const updateArticleGoodAPI = (id) => {
  return request.post(`/article/good/${id}`)
}
export const updateArticleBadAPI = (id) => {
  return request.post(`/article/bad/${id}`)
}

// 同步Redis数据到MySQL
export const syncRedisToMysqlAPI = () => {
  return request.post('/article/sync')
}

// 知识模块 - 获取自己的文章
export const getOwnArticlesAPI = () => {
  return request({
    url: '/knowledge/own',
    method: 'GET'
  })
}

// 知识模块 - 分页获取自己的文章
export const getOwnArticlesPageAPI = (page, pageSize) => {
  return request({
    url: '/knowledge/own/page',
    method: 'GET',
    params: { page, pageSize }
  })
}

// 知识模块 - 获取收藏的文章
export const getCollectionArticlesAPI = () => {
  return request({
    url: '/knowledge/collection',
    method: 'GET'
  })
}

// 知识模块 - 取消收藏
export const deleteCollectionAPI = (articleId) => {
  return request({
    url: '/knowledge/collection',
    method: 'DELETE',
    data: { articleId }
  })
}

// 删除文章
export const deleteArticleAPI = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'DELETE'
  })
}


