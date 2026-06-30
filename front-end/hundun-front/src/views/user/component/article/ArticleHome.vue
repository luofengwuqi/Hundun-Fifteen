<script setup>
import { ref, onMounted, computed } from 'vue'
import { useArticleStore } from '@/stores/article'
import ArticleCard from './ArticleCard.vue'

const getType = (index)=>{
  return typeArr[index]
}
const typeArr = ["null","帮助文档","知识分享"]

const articleStore = useArticleStore()

const loading = ref(true)

const searchText = ref('')
const sortKey = ref('newest')
const belongingFilter = ref(null)

const page = ref(1)

onMounted(async () => {
  loading.value = true
  await articleStore.getArticleList(page.value)
  loading.value = false
})

const totalArticles = computed(() => articleStore.articleList.length)
const totalGood = computed(() =>
  articleStore.articleList.reduce((s, a) => s + (a.good || 0), 0)
)

const belongingOptions = computed(() => {
  const set = new Set(articleStore.articleList.map(a => a.belonging).filter(Boolean))
  return [...set].sort()
})

const filteredArticles = computed(() => {
  let list = articleStore.articleList
  if (belongingFilter.value !== null) {
    list = list.filter(a => a.belonging === belongingFilter.value)
  }
  if (searchText.value.trim()) {
    const q = searchText.value.trim().toLowerCase()
    list = list.filter(a => (a.title || '').toLowerCase().includes(q))
  }
  const sorted = [...list]
  if (sortKey.value === 'newest') {
    sorted.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } else if (sortKey.value === 'hottest') {
    sorted.sort((a, b) => (b.good || 0) - (a.good || 0))
  }
  return sorted
})
</script>

<template>
  <div class="community-page">
    <aside class="sidebar">
      <div class="sidebar-inner">
        <div class="brand">
          <div class="brand-icon">⟡</div>
          <h1 class="brand-title">社区</h1>
          <p class="brand-sub">分享 · 交流 · 共鸣</p>
        </div>

        <div class="stats">
          <div class="stat-block">
            <span class="stat-num">{{ totalArticles }}</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-block">
            <span class="stat-num">{{ totalGood }}</span>
            <span class="stat-label">收获点赞</span>
          </div>
        </div>

        <div class="filter-section">
          <div class="filter-group">
            <span class="filter-label">搜索</span>
            <input
              v-model="searchText"
              class="search-input"
              type="text"
              placeholder="搜索文章标题…"
            />
          </div>
          <div class="filter-group">
            <span class="filter-label">排序</span>
            <div class="sort-tabs">
              <button
                class="sort-tab"
                :class="{ active: sortKey === 'newest' }"
                @click="sortKey = 'newest'"
              >最新</button>
              <button
                class="sort-tab"
                :class="{ active: sortKey === 'hottest' }"
                @click="sortKey = 'hottest'"
              >最热</button>
            </div>
          </div>
          <div class="filter-group">
            <span class="filter-label">分类</span>
            <div class="sort-tabs">
              <button
                class="sort-tab"
                :class="{ active: belongingFilter === null }"
                @click="belongingFilter = null"
              >全部</button>
              <button
                v-for="b in belongingOptions"
                :key="b"
                class="sort-tab"
                :class="{ active: belongingFilter === b }"
                @click="belongingFilter = b"
              >
                {{ getType(b) }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <main class="main">
      <div class="main-header">
        <h2 class="feed-title">全部文章</h2>
        <span class="feed-count">{{ filteredArticles.length }} 篇</span>
      </div>

      <div v-if="loading" class="state-box">
        <div class="spinner" />
        <span>加载中...</span>
      </div>

      <div v-else-if="articleStore.articleList.length === 0" class="state-box">
        <span class="empty-icon">📭</span>
        <h3>还没有文章</h3>
        <p>期待第一篇分享</p>
      </div>

      <div v-else-if="filteredArticles.length === 0" class="state-box">
        <span class="empty-icon">🔍</span>
        <h3>没有匹配的文章</h3>
        <p>试试其他关键词</p>
      </div>

      <div v-else class="grid">
        <ArticleCard
          v-for="article in filteredArticles"
          :key="article.id"
          :article="article"
        />
      </div>
    </main>
  </div>
</template>

<style scoped lang="less">
.community-page {
  display: flex;
  height: 100%;
  background: rgba(245, 239, 230, 0.6);
}

.sidebar {
  flex: 0 0 260px;
  padding: 32px 24px;
  background: #FFFCF5;
  border-right: 1px solid #EDE6D8;

  .sidebar-inner {
    position: sticky;
    top: 32px;
  }

  .brand {
    margin-bottom: 32px;

    .brand-icon {
      font-size: 32px;
      color: #C3272B;
      margin-bottom: 8px;
    }

    .brand-title {
      font-size: 28px;
      font-weight: 700;
      color: #2C1810;
      margin: 0 0 6px 0;
      letter-spacing: 4px;
    }

    .brand-sub {
      font-size: 13px;
      color: #999;
      margin: 0;
      letter-spacing: 2px;
    }
  }

  .stats {
    display: flex;
    gap: 16px;
    padding: 20px 16px;
    background: #F8F2E8;
    border-radius: 12px;
    margin-bottom: 24px;

    .stat-block {
      flex: 1;
      text-align: center;

      .stat-num {
        display: block;
        font-size: 24px;
        font-weight: 700;
        color: #2C1810;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .intro {
    font-size: 13px;
    line-height: 1.7;
    color: #888;
    padding: 0 4px;
  }

  .filter-section {
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #EDE6D8;

    .filter-group {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-label {
        display: block;
        font-size: 11px;
        font-weight: 600;
        color: #BBB;
        text-transform: uppercase;
        letter-spacing: 1px;
        margin-bottom: 8px;
      }

      .search-input {
        width: 100%;
        box-sizing: border-box;
        padding: 8px 12px;
        border: 1px solid #EDE6D8;
        border-radius: 8px;
        font-size: 13px;
        color: #2C1810;
        background: #F8F2E8;
        outline: none;
        transition: all 0.2s;

        &::placeholder {
          color: #CCC;
        }

        &:focus {
          border-color: #C3272B;
          background: #FFFCF5;
        }
      }

      .sort-tabs {
        display: flex;
        gap: 8px;

        .sort-tab {
          flex: 1;
          padding: 6px 0;
          border: 1px solid #EDE6D8;
          border-radius: 8px;
          font-size: 13px;
          color: #999;
          background: #F8F2E8;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            border-color: #DDD;
            color: #666;
          }

          &.active {
            border-color: #C3272B;
            color: #C3272B;
            background: rgba(195, 39, 43, 0.06);
            font-weight: 500;
          }
        }
      }
    }
  }
}

.main {
  flex: 1;
  overflow-y: auto;
  padding: 32px 40px;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: #DDD6C8;
    border-radius: 3px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }

  .main-header {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 28px;
    width: 50%;
    margin-left: auto;
    margin-right: auto;

    .feed-title {
      font-size: 20px;
      font-weight: 600;
      color: #2C1810;
      margin: 0;
    }

    .feed-count {
      font-size: 13px;
      color: #BBB;
    }
  }

  .state-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
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

    p {
      margin: 0;
      font-size: 14px;
    }
  }

  .grid {
    width: 50%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
}

@keyframes spin {
  to { transform: rotate(360deg) }
}
</style>
