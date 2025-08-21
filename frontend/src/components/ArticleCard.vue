<template>
  <div class="article-card" @click="openArticle">
    <img 
      :src="article.imageUrl || defaultImage" 
      :alt="article.title"
      class="article-image"
      @error="onImageError"
    />
    
    <div class="article-content">
      <h3 class="article-title">{{ article.title }}</h3>
      
      <p class="article-summary" v-if="article.summary">
        {{ article.summary }}
      </p>
      
      <div class="article-meta">
        <div class="meta-left">
          <span class="article-source">{{ article.source }}</span>
          <span class="article-category">{{ article.category }}</span>
        </div>
        
        <div class="meta-right">
          <span class="article-time">{{ formatTime(article.publishedAt) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { formatRelativeTime } from '../utils/dateUtils'

export default {
  name: 'ArticleCard',
  
  props: {
    article: {
      type: Object,
      required: true
    }
  },
  
  data() {
    return {
      defaultImage: 'https://via.placeholder.com/350x200/f8f9fa/666666?text=뉴스+이미지'
    }
  },
  
  methods: {
    openArticle() {
      if (this.article.link) {
        window.open(this.article.link, '_blank', 'noopener,noreferrer')
      }
    },
    
    onImageError(event) {
      event.target.src = this.defaultImage
    },
    
    formatTime(dateString) {
      return formatRelativeTime(dateString)
    }
  }
}
</script>

<style scoped>
.article-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.article-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-card:hover .article-image {
  transform: scale(1.05);
}

.article-content {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  line-height: 1.4;
  color: #2d3748;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
  margin-bottom: 1rem;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  font-size: 0.85rem;
  gap: 1rem;
  margin-top: auto;
}

.meta-left {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.meta-right {
  text-align: right;
}

.article-source {
  background: #f1f3f4;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: 500;
  color: #2d3748;
  font-size: 0.8rem;
  display: inline-block;
}

.article-category {
  color: #667eea;
  font-weight: 500;
  font-size: 0.8rem;
}

.article-time {
  color: #999;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .article-content {
    padding: 1rem;
  }
  
  .article-title {
    font-size: 1rem;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .meta-right {
    text-align: left;
  }
}
</style>
