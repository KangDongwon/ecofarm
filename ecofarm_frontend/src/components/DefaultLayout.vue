<template>
    <div class="default-layout">
      <!-- 타이틀 바 -->
      <TitleBar />
      <!-- 네비게이션 바 -->
      <NavigationBar />
      
      <!-- 라우트된 컴포넌트는 여기에 렌더링 -->
      <router-view />
    </div>
  </template>
  
  <script>
  import TitleBar from './TitleBar.vue';
  import NavigationBar from './NavigationBar.vue';
  
  export default {
    components: {
      TitleBar,
      NavigationBar,
    },
  };
  </script>
  
  <style scoped>
  .default-layout {
    display: flex;
    flex-direction: column;
    height: 100vh;
  }
  </style>
  