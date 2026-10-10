import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';

// Vuex 스토어 초기화
store.dispatch('initialize');

createApp(App)
  .use(store)
  .use(router)
  .mount('#app');
