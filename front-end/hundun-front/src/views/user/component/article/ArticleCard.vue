<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  article: { type: Object, required: true }
})

const router = useRouter()

const preview = computed(() => {
  const t = props.article.text || ''
  return t.length > 120 ? t.slice(0, 120) + '…' : t
})

const belongingLabel = computed(() => {
  const map = { 1: '帮助文档' }
  return map[props.article.belonging] || null
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}.${m}.${day}`
}

const goDetail = () => {
  router.push(`/user/article/detail/${props.article.id}`)
}
</script>

<template>
  <div class="card" tabindex="0" @click="goDetail">
    <div class="card-accent" />
    <div class="card-body">
      <div class="card-head">
        <h3 class="card-title">{{ article.title }}</h3>
        <span v-if="belongingLabel" class="belonging-tag">{{ belongingLabel }}</span>
      </div>
      <p class="card-text">{{ preview }}</p>
    </div>
    <div class="card-footer">
      <div class="card-author">
        <span class="article-id">#{{ article.id }}</span>
        <span class="author-name">{{ article.creator || '匿名' }}</span>
        <span class="author-date">{{ formatDate(article.createdAt) }}</span>
      </div>
      <div class="card-stats">
        <span class="stat" title="点赞">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
          {{ article.good || 0 }}
        </span>
        <span class="stat" title="点踩">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3H10zM17 2h2.28a2 2 0 0 1 2 1.7l1.38 9a2 2 0 0 1-2 2.3H17"/></svg>
          {{ article.bad || 0 }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.card {
  background: #FFFCF5;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 1px 6px rgba(0, 0, 0, 0.06),
    0 1px 2px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-3px);
    box-shadow:
      0 8px 20px rgba(0, 0, 0, 0.08),
      0 3px 8px rgba(0, 0, 0, 0.04);
  }

  &:active {
    transform: translateY(-1px);
  }

  .card-accent {
    height: 2px;
    background: linear-gradient(90deg, #C3272B, #D45A5C);
  }

  .card-body {
    padding: 14px 18px 10px;

    .card-head {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 6px;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #2C1810;
        margin: 0;
        line-height: 1.4;
        flex: 1;
        min-width: 0;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .belonging-tag {
        flex-shrink: 0;
        font-size: 10px;
        font-weight: 600;
        color: #C3272B;
        background: rgba(195, 39, 43, 0.08);
        padding: 2px 8px;
        border-radius: 4px;
        letter-spacing: 0.5px;
        white-space: nowrap;
      }
    }

    .card-text {
      font-size: 13px;
      line-height: 1.55;
      color: #888;
      margin: 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 18px 12px;

    .card-author {
      display: flex;
      align-items: center;
      gap: 8px;

      .article-id {
        font-size: 10px;
        font-weight: 600;
        color: #CCC;
        letter-spacing: 0.5px;
      }

      .author-name {
        font-size: 12px;
        font-weight: 500;
        color: #C3272B;
      }

      .author-date {
        font-size: 11px;
        color: #CCC;
      }
    }

    .card-stats {
      display: flex;
      gap: 12px;

      .stat {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: 11px;
        color: #BBB;
        transition: color 0.2s;

        svg {
          opacity: 0.5;
        }
      }
    }
  }
}
</style>
