import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Home from './views/Home.vue'

// CSS 스타일
import './style.css'

// 라우터 설정
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    }
  ]
})

const app = createApp(App)
app.use(router)
app.mount('#app')
