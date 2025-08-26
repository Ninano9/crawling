<template>
  <div class="filters">
    <div class="container">
      <div class="filter-section">
        <h4 class="filter-title">카테고리</h4>
        <div class="filter-group">
          <button 
            class="filter-button"
            :class="{ active: selectedCategory === null }"
            @click="selectCategory(null)"
          >
            전체
          </button>
          <button 
            v-for="category in categories"
            :key="category"
            class="filter-button"
            :class="{ active: selectedCategory === category }"
            @click="selectCategory(category)"
          >
            {{ category }}
          </button>
        </div>
      </div>
      
      <div class="filter-section">
        <h4 class="filter-title">출처</h4>
        <div class="filter-group">
          <button 
            class="filter-button"
            :class="{ active: selectedSource === null }"
            @click="selectSource(null)"
          >
            전체
          </button>
          <button 
            v-for="source in sources"
            :key="source"
            class="filter-button"
            :class="{ active: selectedSource === source }"
            @click="selectSource(source)"
          >
            {{ source }}
          </button>
        </div>
      </div>
      
      <div class="filter-actions">
        <button 
          class="action-button refresh-button"
          @click="refreshData"
          :disabled="isLoading"
        >
          <span v-if="isLoading">🔄</span>
          <span v-else>새로고침</span>
        </button>
        
        <button 
          class="action-button crawl-button"
          @click="startCrawling"
          :disabled="isCrawling"
        >
          <span v-if="isCrawling">🔄 수집 중...</span>
          <span v-else>🕷️ 수집집 시작</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FilterBar',
  
  props: {
    categories: {
      type: Array,
      default: () => []
    },
    sources: {
      type: Array,
      default: () => []
    },
    selectedCategory: {
      type: String,
      default: null
    },
    selectedSource: {
      type: String,
      default: null
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    isCrawling: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['filter-change', 'refresh', 'start-crawling'],
  
  methods: {
    selectCategory(category) {
      this.$emit('filter-change', { category, source: this.selectedSource })
    },
    
    selectSource(source) {
      this.$emit('filter-change', { category: this.selectedCategory, source })
    },
    
    refreshData() {
      this.$emit('refresh')
    },
    
    startCrawling() {
      this.$emit('start-crawling')
    }
  }
}
</script>

<style scoped>
.filters {
  background: white;
  padding: 1.5rem 0;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.filter-section {
  margin-bottom: 1.5rem;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.filter-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-group {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
}

.filter-button {
  padding: 0.5rem 1rem;
  border: 2px solid #dee2e6;
  background: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}

.filter-button:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-1px);
}

.filter-button.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.filter-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f1f3f4;
}

.action-button {
  padding: 0.6rem 1.2rem;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  min-width: 120px;
}

.refresh-button {
  background: #f8f9fa;
  color: #495057;
  border-color: #dee2e6;
}

.refresh-button:hover:not(:disabled) {
  background: #e9ecef;
  border-color: #ced4da;
}

.crawl-button {
  background: linear-gradient(135deg, #28a745, #20c997);
  color: white;
  border-color: #28a745;
}

.crawl-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
}

.action-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 768px) {
  .filters {
    padding: 1rem 0;
  }
  
  .filter-group {
    justify-content: flex-start;
    gap: 0.5rem;
  }
  
  .filter-button {
    font-size: 0.8rem;
    padding: 0.4rem 0.8rem;
  }
  
  .filter-actions {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .action-button {
    font-size: 0.8rem;
    min-width: 100px;
  }
}

@media (max-width: 480px) {
  .filter-section {
    margin-bottom: 1rem;
  }
  
  .filter-title {
    font-size: 0.8rem;
    margin-bottom: 0.5rem;
  }
  
  .filter-group {
    gap: 0.4rem;
  }
  
  .filter-button {
    font-size: 0.75rem;
    padding: 0.35rem 0.7rem;
  }
}
</style>
