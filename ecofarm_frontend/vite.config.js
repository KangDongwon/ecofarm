import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path'; // path 모듈 추가

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  plugins: [vue()],
})
