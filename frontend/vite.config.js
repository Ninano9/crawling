import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: true
  },
  preview: {
    port: 5000,
    host: true,
    strictPort: true,
    allowedHosts: ['crawling-jejy.onrender.com', 'localhost', '127.0.0.1']
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
