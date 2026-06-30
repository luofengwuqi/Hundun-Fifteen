import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getArticleListAPI, updateArticleGoodAPI, updateArticleBadAPI } from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articleList = ref([])

  const currentVoteStatus = ref({})

  const getArticleList = async (page) => {
    const res = await getArticleListAPI(page)
    articleList.value = res.data
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
    articleList, currentVoteStatus,
    getArticleList, setVoteStatus, getVoteStatus,
    toggleGood, toggleBad
  }
})
