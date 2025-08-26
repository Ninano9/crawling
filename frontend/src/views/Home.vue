<template>
  <div class="home">
    <!-- 헤더 -->
    <HeaderComponent 
      :last-update="lastUpdateTime"
      @search="handleSearch"
    />
    
    <!-- 필터 바 -->
    <FilterBar 
      :categories="availableCategories"
      :sources="availableSources"
      :selected-category="currentFilters.category"
      :selected-source="currentFilters.source"
      :is-loading="isLoading"
      :is-crawling="isCrawling"
      @filter-change="handleFilterChange"
      @refresh="refreshArticles"
      @start-crawling="startCrawling"
    />
    
    <!-- 메인 콘텐츠 -->
    <main class="main-content">
      <div class="container">
        <!-- 상태 메시지 -->
        <div v-if="statusMessage" class="status-message" :class="statusType">
          {{ statusMessage }}
        </div>
        
        <!-- 로딩 상태 -->
        <LoadingSpinner 
          v-if="isLoading && articles.length === 0"
          :message="loadingMessage"
        />
        
        <!-- 기사 목록 -->
        <div v-else-if="articles.length > 0" class="articles-section">
          <div class="articles-header">
            <h2 class="articles-title">
              {{ getArticlesTitle() }}
            </h2>
            <p class="articles-count">
              총 {{ totalCount }}개의 기사
            </p>
          </div>
          
          <div class="articles-grid">
            <ArticleCard 
              v-for="article in articles"
              :key="article.id"
              :article="article"
            />
          </div>
          
          <!-- 페이지네이션 -->
          <div v-if="totalPages > 1" class="pagination">
            <button 
              class="pagination-button"
              :disabled="currentPage <= 1"
              @click="changePage(currentPage - 1)"
            >
              이전
            </button>
            
            <span class="pagination-info">
              {{ currentPage }} / {{ totalPages }}
            </span>
            
            <button 
              class="pagination-button"
              :disabled="currentPage >= totalPages"
              @click="changePage(currentPage + 1)"
            >
              다음
            </button>
          </div>
        </div>
        
        <!-- 빈 상태 -->
        <div v-else class="empty-state">
          <div class="empty-icon">📭</div>
          <h3>기사가 없습니다</h3>
          <p>{{ getEmptyMessage() }}</p>
          <button class="refresh-button" @click="refreshArticles">
            새로고침
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import HeaderComponent from '../components/HeaderComponent.vue'
import FilterBar from '../components/FilterBar.vue'
import ArticleCard from '../components/ArticleCard.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import { articleAPI, crawlerAPI } from '../services/api'

export default {
  name: 'Home',
  
  components: {
    HeaderComponent,
    FilterBar,
    ArticleCard,
    LoadingSpinner
  },
  
  data() {
    return {
      // 기사 데이터
      articles: [],
      totalCount: 0,
      currentPage: 1,
      totalPages: 1,
      
      // 필터 데이터
      availableCategories: [],
      availableSources: [],
      currentFilters: {
        category: null,
        source: null
      },
      searchKeyword: '',
      
      // 상태
      isLoading: false,
      isCrawling: false,
      lastUpdateTime: null,
      statusMessage: '',
      statusType: 'info', // 'info', 'success', 'error'
      
      // 설정
      pageSize: 20
    }
  },
  
  computed: {
    loadingMessage() {
      if (this.isCrawling) return '뉴스를 크롤링하는 중...'
      if (this.searchKeyword) return '검색 중...'
      return '기사를 불러오는 중...'
    }
  },
  
  async mounted() {
    await this.initializeData()
  },
  
  methods: {
    async initializeData() {
      this.isLoading = true
      
      try {
        // 병렬로 데이터 로드
        await Promise.all([
          this.loadArticles(),
          this.loadCategories(),
          this.loadSources()
        ])
        
        this.showStatus('데이터를 성공적으로 불러왔습니다.', 'success')
      } catch (error) {
        console.error('초기 데이터 로드 실패:', error)
        this.showStatus('데이터를 불러오는 중 오류가 발생했습니다.', 'error')
      } finally {
        this.isLoading = false
      }
    },
    
    async loadArticles() {
      try {
        let response
        
        if (this.searchKeyword) {
          response = await articleAPI.searchArticles(
            this.searchKeyword, 
            this.currentPage - 1, 
            this.pageSize
          )
        } else {
          response = await articleAPI.getFilteredArticles(
            this.currentFilters,
            this.currentPage - 1,
            this.pageSize
          )
        }
        
        const data = response.data
        this.articles = data.articles || []
        this.totalCount = data.totalCount || 0
        this.totalPages = data.totalPages || 1
        this.currentPage = data.currentPage || 1
        
        // 마지막 업데이트 시간 설정
        if (this.articles.length > 0) {
          this.lastUpdateTime = this.articles[0].createdAt
        }
        
      } catch (error) {
        console.error('기사 로드 실패:', error)
        throw error
      }
    },
    
    async loadCategories() {
      try {
        const response = await articleAPI.getCategories()
        this.availableCategories = response.data || []
      } catch (error) {
        console.error('카테고리 로드 실패:', error)
      }
    },
    
    async loadSources() {
      try {
        const response = await articleAPI.getSources()
        this.availableSources = response.data || []
      } catch (error) {
        console.error('출처 로드 실패:', error)
      }
    },
    
    async handleSearch(keyword) {
      this.searchKeyword = keyword
      this.currentPage = 1
      this.currentFilters = { category: null, source: null }
      
      this.isLoading = true
      try {
        await this.loadArticles()
        
        if (keyword) {
          this.showStatus(`"${keyword}" 검색 결과 ${this.totalCount}개`, 'info')
        }
      } catch (error) {
        this.showStatus('검색 중 오류가 발생했습니다.', 'error')
      } finally {
        this.isLoading = false
      }
    },
    
    async handleFilterChange(filters) {
      this.currentFilters = { ...filters }
      this.currentPage = 1
      this.searchKeyword = ''
      
      this.isLoading = true
      try {
        await this.loadArticles()
      } catch (error) {
        this.showStatus('필터링 중 오류가 발생했습니다.', 'error')
      } finally {
        this.isLoading = false
      }
    },
    
    async changePage(page) {
      if (page < 1 || page > this.totalPages) return
      
      this.currentPage = page
      this.isLoading = true
      
      try {
        await this.loadArticles()
        // 페이지 상단으로 스크롤
        window.scrollTo({ top: 0, behavior: 'smooth' })
      } catch (error) {
        this.showStatus('페이지 로드 중 오류가 발생했습니다.', 'error')
      } finally {
        this.isLoading = false
      }
    },
    
    async refreshArticles() {
      this.currentPage = 1
      this.isLoading = true
      
      try {
        await Promise.all([
          this.loadArticles(),
          this.loadCategories(),
          this.loadSources()
        ])
        
        this.showStatus('새로고침이 완료되었습니다.', 'success')
      } catch (error) {
        this.showStatus('새로고침 중 오류가 발생했습니다.', 'error')
      } finally {
        this.isLoading = false
      }
    },
    
    async startCrawling() {
      this.isCrawling = true
      
      try {
        const response = await crawlerAPI.startCrawling()
        const data = response.data
        
        if (data.success) {
          this.showStatus(`크롤링 완료! ${data.savedCount}개의 새로운 기사가 수집되었습니다.`, 'success')
          
          // 크롤링 완료 후 기사 목록 새로고침
          setTimeout(() => {
            this.refreshArticles()
          }, 1000)
        } else {
          this.showStatus('크롤링에 실패했습니다.', 'error')
        }
      } catch (error) {
        console.error('크롤링 실패:', error)
        this.showStatus('크롤링 중 오류가 발생했습니다.', 'error')
      } finally {
        this.isCrawling = false
      }
    },
    
    showStatus(message, type = 'info') {
      this.statusMessage = message
      this.statusType = type
      
      // 3초 후 메시지 자동 숨김
      setTimeout(() => {
        this.statusMessage = ''
      }, 3000)
    },
    
    getArticlesTitle() {
      if (this.searchKeyword) {
        return `"${this.searchKeyword}" 검색 결과`
      }
      
      const parts = []
      if (this.currentFilters.category) parts.push(this.currentFilters.category)
      if (this.currentFilters.source) parts.push(this.currentFilters.source)
      
      if (parts.length > 0) {
        return parts.join(' - ') + ' 기사'
      }
      
      return '오늘의 뉴스'
    },
    
    getEmptyMessage() {
      if (this.searchKeyword) {
        return `"${this.searchKeyword}"에 대한 검색 결과가 없습니다.`
      }
      
      if (this.currentFilters.category || this.currentFilters.source) {
        return '선택한 조건에 맞는 기사가 없습니다.'
      }
      
      return '아직 수집된 기사가 없습니다. 크롤링을 시작해보세요.'
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.main-content {
  padding-bottom: 3rem;
}

.status-message {
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
}

.status-message.info {
  background: #e3f2fd;
  color: #1565c0;
  border: 1px solid #bbdefb;
}

.status-message.success {
  background: #e8f5e8;
  color: #2e7d2e;
  border: 1px solid #c8e6c9;
}

.status-message.error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

.articles-section {
  margin-bottom: 2rem;
}

.articles-header {
  margin-bottom: 2rem;
  text-align: center;
}

.articles-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 0.5rem;
}

.articles-count {
  color: #666;
  font-size: 1rem;
  margin: 0;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin: 2rem 0;
}

.pagination-button {
  padding: 0.75rem 1.5rem;
  border: 2px solid #7a7a7a;
  background: white;
  color: #7a7a7a;
  cursor: pointer;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.pagination-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #9d9d9d 0%, #4b4b4b 100%);
  color: white;
  transform: translateY(-1px);
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.pagination-info {
  font-weight: 600;
  color: #495057;
  min-width: 80px;
  text-align: center;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: #2d3748;
}

.empty-state p {
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.refresh-button {
  padding: 0.75rem 2rem;
  background: linear-gradient(135deg, #9d9d9d 0%, #4b4b4b 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-button:hover {
  background: linear-gradient(135deg, #8a8a8a 0%, #3a3a3a 100%);
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .articles-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .articles-title {
    font-size: 1.5rem;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .pagination-button {
    padding: 0.6rem 1.2rem;
    font-size: 0.9rem;
  }
}
</style>
