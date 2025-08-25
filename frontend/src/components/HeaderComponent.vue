<template>
  <header class="header">
    <div class="container">
      <div class="header-content">
        <div class="logo">
          📰 뉴스 자동수집 블로그
        </div>
        
        <div class="search-container">
          <input 
            type="text" 
            v-model="searchQuery"
            @keyup.enter="handleSearch"
            @input="onSearchInput"
            placeholder="기사 검색..."
            class="search-input"
          />
        </div>
        
        <div class="header-info">
          <div class="date-info">
            {{ todayString }}
          </div>
          <div class="update-info" v-if="lastUpdateTime">
            마지막 업데이트: {{ lastUpdateTime }}
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { getTodayString, formatRelativeTime } from '../utils/dateUtils'

export default {
  name: 'HeaderComponent',
  
  props: {
    lastUpdate: {
      type: String,
      default: null
    }
  },
  
  emits: ['search'],
  
  data() {
    return {
      searchQuery: '',
      searchTimeout: null
    }
  },
  
  computed: {
    todayString() {
      return getTodayString()
    },
    
    lastUpdateTime() {
      return this.lastUpdate ? formatRelativeTime(this.lastUpdate) : null
    }
  },
  
  methods: {
    handleSearch() {
      this.$emit('search', this.searchQuery.trim())
    },
    
    onSearchInput() {
      // 디바운싱: 입력 후 500ms 대기
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout)
      }
      
      this.searchTimeout = setTimeout(() => {
        if (this.searchQuery.trim().length >= 2) {
          this.handleSearch()
        } else if (this.searchQuery.trim().length === 0) {
          this.$emit('search', '') // 검색어가 비어있으면 전체 조회
        }
      }, 500)
    }
  },
  
  beforeUnmount() {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout)
    }
  }
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  white-space: nowrap;
}

.search-container {
  flex: 1;
  max-width: 400px;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 50px;
  font-size: 1rem;
  outline: none;
  transition: box-shadow 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 0 0 3px rgba(255,255,255,0.3);
}

.header-info {
  text-align: right;
  font-size: 0.9rem;
  opacity: 0.9;
  min-width: 200px;
}

.date-info {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.update-info {
  font-size: 0.8rem;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .search-container {
    max-width: none;
    width: 100%;
    order: 3;
  }
  
  .header-info {
    text-align: center;
    min-width: auto;
    order: 2;
  }
  
  .logo {
    order: 1;
    font-size: 1.25rem;
  }
}

@media (max-width: 480px) {
  .logo {
    font-size: 1.1rem;
  }
  
  .search-input {
    font-size: 0.9rem;
    padding: 0.6rem 0.8rem;
  }
  
  .header-info {
    font-size: 0.8rem;
  }
}
</style>
