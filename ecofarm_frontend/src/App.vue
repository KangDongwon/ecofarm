<template>
  <div id="app">
    <!-- MainPage 컴포넌트를 직접 렌더링하는 대신, router-view를 사용하여 라우팅된 컴포넌트를 렌더링 -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* 전역 스타일 */
</style>
