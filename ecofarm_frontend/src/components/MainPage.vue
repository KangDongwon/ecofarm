<template>
  <div class="main-page">
    <!-- 타이틀 바 -->
    <TitleBar />
    <!-- 내비게이션 바 -->
    <NavigationBar />
    <!-- 메인 이미지와 텍스트 -->
    <div class="content">
      <img class="main-image" src="../assets/home1.png" alt="메인 이미지" />
      <img class="main-image" src="../assets/home2.png" alt="메인 이미지" />
    </div>
    <footer class="footer">
      <p>© 2024 EcoFarm Co., Ltd. All rights reserved.</p>
    </footer>
  </div>
</template>

<script>
import TitleBar from './TitleBar.vue';
import NavigationBar from './NavigationBar.vue';

export default {
  components: {
    TitleBar,
    NavigationBar
  }
}
</script>

<style scoped>
.main-page {
  display: flex;
  flex-direction: column;
  height: 100vh; /* 뷰포트 높이 100% 설정 */
}

.content {
  margin-top: 10px;
  flex-direction: column;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgb(245, 245, 217);
}

.main-image {
  margin-top: 50px;
  width: 80%;
  height: 100%;
  object-fit: cover;
  margin-bottom: 50px;
}

.footer {
  margin-top: 10px;
  text-align: center;
  padding: 10px;
  background-color: #f1f1f1;
  font-size: 0.9rem;
  color: #555;
}
</style>
