import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getArticleListAPI, updateArticleGoodAPI, updateArticleBadAPI } from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articleList = ref([])
  const total = ref(0)
  const totalPages = ref(0)

  const currentVoteStatus = ref({})

  const getArticleList = async (page = 1, pageSize = 10, belonging = null, articleStatus = null) => {
    const res = await getArticleListAPI(page, pageSize, belonging, articleStatus)
    const pageInfo = res.data
    articleList.value = pageInfo.list || []
    total.value = pageInfo.total || 0
    totalPages.value = pageInfo.pages || 0
    return res
  }

  const setVoteStatus = (id, status) => {
    currentVoteStatus.value[id] = status
  }

  const getVoteStatus = (id) => {
    return currentVoteStatus.value[id] || null
  }

  const toggleGood = async (id) => {
    const prev = currentVoteStatus.value[id]
    await updateArticleGoodAPI(id)
    const article = articleList.value.find(item => item.id === id)
    if (!article) return
    if (prev === 'good') {
      article.good = Math.max(0, (article.good || 0) - 1)
      currentVoteStatus.value[id] = null
    } else {
      if (prev === 'bad') article.bad = Math.max(0, (article.bad || 0) - 1)
      article.good = (article.good || 0) + 1
      currentVoteStatus.value[id] = 'good'
    }
  }

  const toggleBad = async (id) => {
    const prev = currentVoteStatus.value[id]
    await updateArticleBadAPI(id)
    const article = articleList.value.find(item => item.id === id)
    if (!article) return
    if (prev === 'bad') {
      article.bad = Math.max(0, (article.bad || 0) - 1)
      currentVoteStatus.value[id] = null
    } else {
      if (prev === 'good') article.good = Math.max(0, (article.good || 0) - 1)
      article.bad = (article.bad || 0) + 1
      currentVoteStatus.value[id] = 'bad'
    }
  }

  return {
    articleList, total, totalPages, currentVoteStatus,
    getArticleList, setVoteStatus, getVoteStatus,
    toggleGood, toggleBad
  }
})
