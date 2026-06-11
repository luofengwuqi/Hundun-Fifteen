<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useArticleStore } from '@/stores/article'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()

const article = computed(() =>
  articleStore.articleList.find(a => a.id === Number(route.params.id)) || null
)

const loading = ref(false)
const notFound = ref(false)

watch(() => route.params.id, async () => {
  const id = Number(route.params.id)
  let found = articleStore.articleList.find(a => a.id === id)
  if (!found) {
    loading.value = true
    await articleStore.getArticleList()
    found = articleStore.articleList.find(a => a.id === id)
    loading.value = false
  }
  notFound.value = !found
}, { immediate: true })

const voteStatus = computed(() => articleStore.getVoteStatus(article.value?.id))

const handleGood = async () => {
  if (!article.value) return
  await articleStore.toggleGood(article.value.id)
}

const handleBad = async () => {
  if (!article.value) return
  await articleStore.toggleBad(article.value.id)
}

const renderedContent = computed(() => {
  if (!article.value) return ''
  return marked(article.value.text || '')
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${day}日 ${h}:${min}`
}

const belongingLabel = computed(() => {
  const map = { 1: '帮助文档' }
  return map[article.value?.belonging] || null
})

const goBack = () => {
  router.push('/user/article')
}
</script>

<template>
  <div class="detail-page">
    <div v-if="loading" class="state-box">
      <div class="spinner" />
      <span>加载中...</span>
    </div>

    <div v-else-if="notFound || !article" class="state-box">
      <span class="empty-icon">📭</span>
      <h3>文章不存在</h3>
      <button class="back-btn" @click="goBack">返回社区</button>
    </div>

    <template v-else>
      <div class="back-bar">
        <button class="back-link" @click="goBack">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回社区
        </button>
      </div>

      <article class="article">
        <header class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <span v-if="belongingLabel" class="belonging-tag">{{ belongingLabel }}</span>
          <div class="header-meta">
            <span class="article-id">#{{ article.id }}</span>
            <span class="article-author">{{ article.creator || '匿名' }}</span>
            <span class="article-sep">·</span>
            <span class="article-date">{{ formatDate(article.createdAt) }}</span>
          </div>
        </header>

        <div class="article-content" v-html="renderedContent" />

        <div class="article-actions">
          <button class="report-btn" title="举报" @click="router.push('/help')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 9v4m0 4h.01M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"/>
            </svg>
          </button>
          <button
            class="action-btn like"
            :class="{ active: voteStatus === 'good' }"
            @click="handleGood"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" :fill="voteStatus === 'good' ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
            </svg>
            <span>{{ article.good || 0 }}</span>
          </button>
          <button
            class="action-btn dislike"
            :class="{ active: voteStatus === 'bad' }"
            @click="handleBad"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" :fill="voteStatus === 'bad' ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3H10zM17 2h2.28a2 2 0 0 1 2 1.7l1.38 9a2 2 0 0 1-2 2.3H17"/>
            </svg>
            <span>{{ article.bad || 0 }}</span>
          </button>
        </div>

        <div class="comment-section">
          <div class="comment-header">
            <h3 class="comment-title">评论区</h3>
            <span class="comment-placeholder">评论功能即将上线</span>
          </div>
        </div>
      </article>
    </template>
  </div>
</template>

<style scoped lang="less">
.detail-page {
  min-height: 100%;
  background: rgba(245, 239, 230, 0.6);
  padding-bottom: 80px;
}

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 20px;
  color: #999;
  gap: 12px;

  .spinner {
    width: 32px;
    height: 32px;
    border: 3px solid #EDE6D8;
    border-top-color: #C3272B;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .empty-icon {
    font-size: 48px;
  }

  h3 {
    margin: 0;
    font-size: 18px;
    color: #666;
  }

  .back-btn {
    margin-top: 8px;
    padding: 8px 24px;
    border: 1px solid #DDD6C8;
    border-radius: 8px;
    background: #FFFCF5;
    color: #666;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #C3272B;
      color: #C3272B;
    }
  }
}

.back-bar {
  width: 70%;
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 0 0;

  .back-link {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    border: 1px solid #EDE6D8;
    border-radius: 8px;
    background: #FFFCF5;
    color: #999;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #C3272B;
      color: #C3272B;
      background: rgba(195, 39, 43, 0.04);
    }
  }
}

.article {
  width: 70%;
  max-width: 860px;
  margin: 20px auto 0;
  background: #FFFCF5;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  .article-header {
    position: relative;
    padding: 32px 40px;
    border-bottom: 1px solid #F0E8D8;

    .article-title {
      font-size: 22px;
      font-weight: 700;
      color: #2C1810;
      margin: 0;
      line-height: 1.35;
      letter-spacing: 1px;
      text-align: center;
    }

    .belonging-tag {
      position: absolute;
      top: 50%;
      right: 40px;
      transform: translateY(-50%);
      font-size: 10px;
      font-weight: 600;
      color: #C3272B;
      background: rgba(195, 39, 43, 0.08);
      padding: 3px 10px;
      border-radius: 4px;
      letter-spacing: 0.5px;
    }

    .header-meta {
      position: absolute;
      bottom: 12px;
      right: 40px;
      display: flex;
      align-items: center;
      gap: 6px;

      .article-id {
        font-size: 11px;
        font-weight: 500;
        color: #CCC;
      }

      .article-author {
        font-size: 12px;
        font-weight: 500;
        color: #C3272B;
      }

      .article-sep {
        color: #DDD;
      }

      .article-date {
        font-size: 12px;
        color: #BBB;
      }
    }
  }

  .article-content {
    padding: 40px 56px 32px;
    font-size: 16px;
    line-height: 1.85;
    color: #3A3A3A;

    :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
      color: #2C1810;
      font-weight: 600;
      margin: 1.5em 0 0.6em;
      line-height: 1.35;
    }

    :deep(h1) { font-size: 1.6em; }
    :deep(h2) { font-size: 1.35em; }
    :deep(h3) { font-size: 1.15em; }

    :deep(p) {
      margin: 0 0 1em;
    }

    :deep(ul), :deep(ol) {
      margin: 0 0 1em;
      padding-left: 1.5em;
    }

    :deep(li) {
      margin-bottom: 0.3em;
    }

    :deep(blockquote) {
      margin: 1em 0;
      padding: 12px 20px;
      background: #F8F2E8;
      border-left: 3px solid #C3272B;
      border-radius: 0 6px 6px 0;
      color: #666;

      p {
        margin: 0;
      }
    }

    :deep(code) {
      font-size: 0.88em;
      padding: 2px 6px;
      background: #F0E8D8;
      border-radius: 4px;
      color: #C3272B;
    }

    :deep(pre) {
      margin: 1em 0;
      padding: 16px 20px;
      background: #2C1810;
      border-radius: 8px;
      overflow-x: auto;

      code {
        background: none;
        padding: 0;
        color: #EDE6D8;
        font-size: 0.85em;
      }
    }

    :deep(img) {
      max-width: 100%;
      border-radius: 8px;
      margin: 1em 0;
    }

    :deep(a) {
      color: #C3272B;
      text-decoration: none;
      border-bottom: 1px solid rgba(195, 39, 43, 0.3);

      &:hover {
        border-bottom-color: #C3272B;
      }
    }

    :deep(hr) {
      border: none;
      height: 1px;
      background: #EDE6D8;
      margin: 2em 0;
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin: 1em 0;

      th, td {
        padding: 10px 14px;
        border: 1px solid #EDE6D8;
        text-align: left;
      }

      th {
        background: #F8F2E8;
        font-weight: 600;
        color: #2C1810;
      }
    }
  }

  .article-actions {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 8px;
    padding: 8px 40px 16px;

    .report-btn {
      width: 30px;
      height: 30px;
      border-radius: 50%;
      border: 1px solid #DDD6C8;
      background: #FFFCF5;
      color: #CCC;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s;

      &:hover {
        border-color: #999;
        color: #999;
      }
    }

    .action-btn {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 6px 18px;
      border-radius: 20px;
      border: 1px solid #DDD6C8;
      background: #FFFCF5;
      color: #888;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: #C3272B;
        color: #C3272B;
        background: rgba(195, 39, 43, 0.04);
      }

      &.active {
        border-color: #C3272B;
        color: #C3272B;
        background: rgba(195, 39, 43, 0.08);
      }

      &.dislike.active {
        color: #888;
        border-color: #888;
        background: rgba(0,0,0,0.04);
      }
    }
  }

  .comment-section {
    border-top: 1px solid #F0E8D8;
    padding: 32px 56px 48px;

    .comment-header {
      display: flex;
      align-items: baseline;
      gap: 12px;

      .comment-title {
        font-size: 18px;
        font-weight: 600;
        color: #2C1810;
        margin: 0;
      }

      .comment-placeholder {
        font-size: 13px;
        color: #CCC;
      }
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg) }
}
</style>
