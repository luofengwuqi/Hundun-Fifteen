<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import {
  getOwnArticlesAPI,
  getCollectionArticlesAPI,
  deleteArticleAPI,
  deleteCollectionAPI,
  updateArticleGoodAPI,
  updateArticleBadAPI
} from '@/api/article'
import FunctionDisabled from './FunctionDisabled.vue'

const userStore = useUserStore()
const router = useRouter()
const enabled = computed(() => userStore.userModelStatus?.knowledge === 1)

const goDetail = (id) => {
  router.push(`/user/article/detail/${id}`)
}

const activeTab = ref('all')
const sortBy = ref('newest')
const loading = ref(false)
const error = ref('')

const ownArticles = ref([])
const collectedArticles = ref([])

const searchKeyword = ref('')

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    if (activeTab.value === 'own') {
      const res = await getOwnArticlesAPI()
      ownArticles.value = res.data || []
    } else if (activeTab.value === 'collected') {
      const res = await getCollectionArticlesAPI()
      collectedArticles.value = res.data || []
    }
  } catch (err) {
    console.error('Failed to load articles:', err)
    error.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deleteArticleAPI(id)
    ownArticles.value = ownArticles.value.filter(a => a.id !== id)
  } catch (err) {
    console.error('Failed to delete article:', err)
  }
}

const handleUnfavorite = async (articleId) => {
  try {
    await deleteCollectionAPI(articleId)
    collectedArticles.value = collectedArticles.value.filter(a => a.id !== articleId)
  } catch (err) {
    console.error('Failed to unfavorite:', err)
  }
}

const handleToggleGood = async (id) => {
  try {
    const target = displayArticles.value.find(a => a.id === id)
    if (!target) return
    await updateArticleGoodAPI(id)
    const prevVote = voteStatus.value[id]
    if (prevVote === 'good') {
      target.good = Math.max(0, (target.good || 0) - 1)
      voteStatus.value[id] = null
    } else {
      if (prevVote === 'bad') target.bad = Math.max(0, (target.bad || 0) - 1)
      target.good = (target.good || 0) + 1
      voteStatus.value[id] = 'good'
    }
  } catch (err) {
    console.error('Failed to toggle good:', err)
  }
}

const handleToggleBad = async (id) => {
  try {
    const target = displayArticles.value.find(a => a.id === id)
    if (!target) return
    await updateArticleBadAPI(id)
    const prevVote = voteStatus.value[id]
    if (prevVote === 'bad') {
      target.bad = Math.max(0, (target.bad || 0) - 1)
      voteStatus.value[id] = null
    } else {
      if (prevVote === 'good') target.good = Math.max(0, (target.good || 0) - 1)
      target.bad = (target.bad || 0) + 1
      voteStatus.value[id] = 'bad'
    }
  } catch (err) {
    console.error('Failed to toggle bad:', err)
  }
}

const voteStatus = ref({})

const filterOptions = [
  { value: 'newest', label: '最新' },
  { value: 'popular', label: '最热' }
]

const tabs = [
  { value: 'own', label: '我的投稿', icon: '✍️' },
  { value: 'collected', label: '我的收藏', icon: '⭐' }
]

const displayArticles = computed(() => {
  let articles = []
  if (activeTab.value === 'own') articles = ownArticles.value
  else if (activeTab.value === 'collected') articles = collectedArticles.value

  let filtered = [...articles]
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(a =>
      a.title?.toLowerCase().includes(kw) ||
      a.text?.toLowerCase().includes(kw)
    )
  }

  if (sortBy.value === 'popular') {
    return filtered.sort((a, b) => (b.good || 0) - (a.good || 0))
  }
  return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
})

const stats = computed(() => ({
  own: ownArticles.value.length,
  collected: collectedArticles.value.length
}))

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}·${m}·${day}`
}

const truncate = (text, len = 120) => {
  if (!text) return ''
  return text.length > len ? text.slice(0, len) + '…' : text
}

const isOwn = (item) => {
  return item.userId === userStore.userInfo?.userId || item.creator === userStore.userInfo?.username
}

onMounted(() => {
  loadData()
})

watch(activeTab, () => {
  loadData()
})
</script>

<template>
  <FunctionDisabled v-if="!enabled" module-name="知识" />
  <div v-else class="wenyuan">
    <div class="wenyuan-bg"></div>
    <div class="wenyuan-header">
      <div class="header-brand">
        <span class="brand-icon">📚</span>
        <div class="brand-text">
          <h2 class="brand-title">文渊阁</h2>
          <p class="brand-sub">Wenyuan Knowledge Archive</p>
        </div>
      </div>
      <div class="header-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
        </button>
      </div>
    </div>

    <div class="wenyuan-body">
      <div class="content-main">
        <div class="content-toolbar">
          <div class="toolbar-left">
            <div class="search-wrap">
              <span class="search-icon">🔍</span>
              <input
                v-model="searchKeyword"
                type="text"
                class="search-input"
                placeholder="搜文章..."
              />
            </div>
          </div>
          <div class="toolbar-right">
            <button
              v-for="opt in filterOptions"
              :key="opt.value"
              class="sort-chip"
              :class="{ active: sortBy === opt.value }"
              @click="sortBy = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <div class="articles-area">
          <TransitionGroup name="card-stagger" tag="div" class="articles-grid">
            <div
              v-for="(article, idx) in displayArticles"
              :key="article.id"
              class="article-card"
              :class="{ clickable: article.id }"
              :style="{ '--i': idx }"
              @click="goDetail(article.id)"
            >
              <div class="card-accent"></div>
              <div class="card-body">
                <div class="card-meta">
                  <span class="meta-author">{{ article.creator || '匿名' }}</span>
                  <span class="meta-dot">·</span>
                  <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
                  <span v-if="isOwn(article)" class="meta-own-badge">我的</span>
                </div>
                <h3 class="card-title">{{ article.title || '无标题' }}</h3>
                <p class="card-excerpt">{{ truncate(article.text) }}</p>
              </div>
              <div class="card-actions">
                <div class="action-group">
                  <button
                    class="action-btn vote-btn"
                    :class="{ liked: voteStatus[article.id] === 'good' }"
                    @click.stop="handleToggleGood(article.id)"
                  >
                    <span class="vote-icon">👍</span>
                    <span class="vote-count">{{ article.good || 0 }}</span>
                  </button>
                  <button
                    class="action-btn vote-btn"
                    :class="{ disliked: voteStatus[article.id] === 'bad' }"
                    @click.stop="handleToggleBad(article.id)"
                  >
                    <span class="vote-icon">👎</span>
                    <span class="vote-count">{{ article.bad || 0 }}</span>
                  </button>
                </div>
                <div class="action-group">
                  <button
                    v-if="activeTab === 'own' || isOwn(article)"
                    class="action-btn delete-btn"
                    @click.stop="handleDelete(article.id)"
                  >
                    <span>🗑️</span>
                    <span>删除</span>
                  </button>
                  <button
                    v-if="activeTab === 'collected'"
                    class="action-btn unfav-btn"
                    @click.stop="handleUnfavorite(article.id)"
                  >
                    <span>💔</span>
                    <span>取消收藏</span>
                  </button>
                </div>
              </div>
            </div>
          </TransitionGroup>

          <div v-if="loading" class="state-box">
            <div class="loading-spinner"></div>
            <p class="state-text">载入中...</p>
          </div>

          <div v-else-if="error" class="state-box">
            <p class="state-text state-error">{{ error }}</p>
          </div>

          <div v-else-if="displayArticles.length === 0" class="state-box">
            <p class="state-text state-empty">📭 此处尚无文章</p>
          </div>
        </div>
      </div>

      <aside class="content-sidebar">
        <div class="sidebar-section">
          <h4 class="sidebar-title">
            <span class="sidebar-title-icon">📊</span>
            <span>总览</span>
          </h4>
          <div class="stat-list">
            <div class="stat-item">
              <span class="stat-label">我的投稿</span>
              <span class="stat-value">{{ stats.own }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">我的收藏</span>
              <span class="stat-value">{{ stats.collected }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-section">
          <h4 class="sidebar-title">
            <span class="sidebar-title-icon">🏷️</span>
            <span>排序</span>
          </h4>
          <div class="sort-list">
            <label
              v-for="opt in filterOptions"
              :key="opt.value"
              class="sort-radio"
              :class="{ checked: sortBy === opt.value }"
            >
              <input
                type="radio"
                name="sortSidebar"
                :value="opt.value"
                v-model="sortBy"
                class="sort-radio-input"
              />
              <span class="sort-radio-dot"></span>
              <span class="sort-radio-label">{{ opt.label }}</span>
            </label>
          </div>
        </div>

        <div class="sidebar-section">
          <h4 class="sidebar-title">
            <span class="sidebar-title-icon">📌</span>
            <span>分类</span>
          </h4>
          <div class="tab-list-sidebar">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              class="sidebar-tab-btn"
              :class="{ active: activeTab === tab.value }"
              @click="activeTab = tab.value"
            >
              <span class="stb-icon">{{ tab.icon }}</span>
              <span class="stb-label">{{ tab.label }}</span>
              <span class="stb-count">
                {{ tab.value === 'own' ? stats.own : stats.collected }}
              </span>
            </button>
          </div>
        </div>

        <div class="sidebar-footer">
          <p class="sidebar-footer-text">学而时习之</p>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped lang="less">
@red: #c3272b;
@red-hover: #d64a4f;
@brown: #5a4a42;
@dark: #2c2c2c;
@cream: #fffbf8;
@cream-soft: #fff8e8;
@cream-dark: #ffefd5;
@gold: #a67c52;
@gold-light: #d4b896;
@text: #3e3e3e;
@text-muted: #999;
@border: #eee8d8;
@shadow: rgba(90, 74, 66, 0.08);
@shadow-strong: rgba(90, 74, 66, 0.15);

.wenyuan {
  --bg: @cream;
  --bg-card: #ffffff;
  --bg-cream: @cream-soft;
  --bg-cream-dark: @cream-dark;
  --text-primary: @dark;
  --text-body: @brown;
  --text-muted: @text-muted;
  --accent: @red;
  --accent-hover: @red-hover;
  --gold: @gold;
  --gold-light: @gold-light;
  --border-color: @border;

  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg);
  color: var(--text-body);
  font-size: 14px;
  line-height: 1.6;
}

.wenyuan-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at 10% 20%, rgba(195, 39, 43, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 90% 80%, rgba(166, 124, 82, 0.04) 0%, transparent 50%);
  z-index: 0;
}

.wenyuan-header {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 28px 14px;
  border-bottom: 1px solid var(--border-color);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  font-size: 26px;
  line-height: 1;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 2px;
  line-height: 1.2;
}

.brand-sub {
  margin: 0;
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 1px;
  text-transform: uppercase;
  font-weight: 400;
}

.header-tabs {
  display: flex;
  gap: 4px;
  background: @cream-soft;
  padding: 3px;
  border-radius: 10px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 7px 16px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;

  &:hover {
    color: var(--text-body);
  }

  &.active {
    background: #fff;
    color: var(--accent);
    font-weight: 600;
    box-shadow: 0 1px 4px rgba(195, 39, 43, 0.12);
  }
}

.tab-icon {
  font-size: 14px;
  line-height: 1;
}

.tab-label {
  line-height: 1;
}

.wenyuan-body {
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 0;
  overflow: hidden;
}

.content-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 28px;
  gap: 16px;
  border-bottom: 1px solid @border;
  background: rgba(255, 255, 255, 0.4);
}

.toolbar-left {
  flex: 1;
  min-width: 0;
}

.search-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: @cream-soft;
  border: 1px solid @border;
  border-radius: 8px;
  max-width: 280px;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus-within {
    border-color: fade(@red, 30%);
    box-shadow: 0 0 0 2px fade(@red, 6%);
  }
}

.search-icon {
  font-size: 13px;
  line-height: 1;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  min-width: 0;
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--text-body);
  outline: none;
  padding: 0;

  &::placeholder {
    color: var(--text-muted);
    font-size: 12px;
  }
}

.toolbar-right {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.sort-chip {
  padding: 5px 14px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: var(--text-body);
    border-color: @border;
  }

  &.active {
    background: @red;
    color: #fff;
    font-weight: 500;
    border-color: @red;
    box-shadow: 0 1px 4px fade(@red, 25%);
  }
}

.articles-area {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 28px;

  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: @border;
    border-radius: 3px;
    &:hover { background: darken(@border, 10%); }
  }
}

.articles-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border: 1px solid @border;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 4px @shadow;

  &.clickable {
    cursor: pointer;
  }

  &:hover {
    border-color: @gold-light;
    box-shadow: 0 4px 20px @shadow-strong;
    transform: translateY(-2px);
  }
}

.card-accent {
  height: 3px;
  background: linear-gradient(90deg, @red, @gold);
  flex-shrink: 0;
}

.card-body {
  padding: 16px 20px 12px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.meta-author {
  font-weight: 500;
  color: var(--text-body);
}

.meta-dot {
  color: @border;
}

.meta-date {
  color: var(--text-muted);
}

.meta-own-badge {
  padding: 1px 7px;
  border-radius: 4px;
  background: fade(@red, 8%);
  color: @red;
  font-size: 10px;
  font-weight: 600;
  margin-left: auto;
}

.card-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-excerpt {
  margin: 0;
  font-size: 13px;
  color: var(--text-body);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  opacity: 0.8;
}

.card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px 12px;
  border-top: 1px solid fade(@border, 50%);
}

.action-group {
  display: flex;
  gap: 6px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  font-size: 12px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: @cream-soft;
    color: var(--text-body);
  }
}

.vote-btn {
  &.liked {
    background: fade(@red, 8%);
    color: @red;
    border-color: fade(@red, 15%);
  }
  &.disliked {
    background: fade(@text-muted, 8%);
    color: @text-muted;
    border-color: fade(@text-muted, 15%);
  }
}

.vote-icon {
  font-size: 12px;
  line-height: 1;
}

.vote-count {
  font-variant-numeric: tabular-nums;
  min-width: 10px;
}

.delete-btn:hover {
  color: @red !important;
  border-color: fade(@red, 20%);
  background: fade(@red, 6%);
}

.unfav-btn:hover {
  color: @gold !important;
  border-color: fade(@gold, 20%);
  background: fade(@gold, 6%);
}

.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 12px;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 2px solid @border;
  border-top-color: @red;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.state-text {
  font-size: 14px;
  color: var(--text-muted);
}

.state-error {
  color: @red;
}

.state-empty {
  color: var(--text-muted);
  font-size: 15px;
}

/* Content sidebar */
.content-sidebar {
  flex: 0 0 180px;
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 20px 16px;
  border-left: 1px solid @border;
  background: rgba(255, 255, 255, 0.35);
  overflow-y: auto;
}

.sidebar-section {
  margin-bottom: 20px;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.sidebar-title-icon {
  font-size: 13px;
}

.stat-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background: @cream-soft;
  border-radius: 6px;
}

.stat-label {
  font-size: 12px;
  color: var(--text-body);
}

.stat-value {
  font-size: 14px;
  font-weight: 700;
  color: @dark;
  font-variant-numeric: tabular-nums;
}

.sort-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sort-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: @cream-soft;
  }

  &.checked {
    background: fade(@red, 6%);
  }
}

.sort-radio-input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.sort-radio-dot {
  width: 14px;
  height: 14px;
  border: 2px solid @border;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.2s;
  position: relative;

  .checked & {
    border-color: @red;

    &::after {
      content: '';
      position: absolute;
      inset: 2px;
      background: @red;
      border-radius: 50%;
    }
  }
}

.sort-radio-label {
  font-size: 13px;
  color: var(--text-body);
}

.tab-list-sidebar {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-tab-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;

  &:hover {
    background: @cream-soft;
    color: var(--text-body);
  }

  &.active {
    background: fade(@red, 8%);
    color: @red;
    font-weight: 600;
  }
}

.stb-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.stb-label {
  flex: 1;
}

.stb-count {
  font-size: 11px;
  font-weight: 500;
  color: inherit;
  opacity: 0.6;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid @border;
  text-align: center;
}

.sidebar-footer-text {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 2px;
  font-style: italic;
}

/* TransitionGroup animations */
.card-stagger-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  transition-delay: calc(var(--i, 0) * 0.05s);
}

.card-stagger-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card-stagger-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.97);
}

.card-stagger-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
