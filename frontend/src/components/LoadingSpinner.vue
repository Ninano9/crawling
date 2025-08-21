<template>
  <div class="loading-container">
    <div class="spinner"></div>
    <p class="loading-text">{{ message }}</p>
  </div>
</template>

<script>
export default {
  name: 'LoadingSpinner',
  
  props: {
    message: {
      type: String,
      default: '데이터를 불러오는 중...'
    }
  }
}
</script>

<style scoped>
.loading-container {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 1rem;
  margin: 0;
}
</style>
