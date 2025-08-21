import axios from 'axios'

// API 기본 설정
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터
api.interceptors.request.use(
  config => {
    console.log(`API 요청: ${config.method?.toUpperCase()} ${config.url}`)
    return config
  },
  error => {
    console.error('API 요청 오류:', error)
    return Promise.reject(error)
  }
)

// 응답 인터셉터
api.interceptors.response.use(
  response => {
    console.log(`API 응답: ${response.status} ${response.config.url}`)
    return response
  },
  error => {
    console.error('API 응답 오류:', error.response?.status, error.message)
    return Promise.reject(error)
  }
)

// 기사 관련 API
export const articleAPI = {
  // 오늘의 기사 조회
  getTodaysArticles(page = 0, size = 20) {
    return api.get(`/api/articles/today?page=${page}&size=${size}`)
  },

  // 기사 검색
  searchArticles(keyword, page = 0, size = 20) {
    return api.get(`/api/articles/search?q=${encodeURIComponent(keyword)}&page=${page}&size=${size}`)
  },

  // 필터링된 기사 조회
  getFilteredArticles(filters = {}, page = 0, size = 20) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    })
    
    if (filters.category) params.append('category', filters.category)
    if (filters.source) params.append('source', filters.source)
    
    return api.get(`/api/articles?${params}`)
  },

  // 기사 상세 조회
  getArticleById(id) {
    return api.get(`/api/articles/${id}`)
  },

  // 전체 카테고리 목록
  getCategories() {
    return api.get('/api/articles/categories')
  },

  // 전체 출처 목록
  getSources() {
    return api.get('/api/articles/sources')
  },

  // 출처별 카테고리 목록
  getCategoriesBySource(source) {
    return api.get(`/api/articles/sources/${encodeURIComponent(source)}/categories`)
  }
}

// 크롤러 관련 API
export const crawlerAPI = {
  // 수동 크롤링 실행
  startCrawling() {
    return api.post('/api/crawler/crawl')
  },

  // 특정 소스 크롤링
  crawlSource(source) {
    return api.post(`/api/crawler/crawl/${encodeURIComponent(source)}`)
  },

  // 크롤링 통계
  getStats() {
    return api.get('/api/crawler/stats')
  },

  // 크롤러 상태
  getStatus() {
    return api.get('/api/crawler/status')
  }
}

export default api
