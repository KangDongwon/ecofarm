<template>
  <nav class="navigation-bar">
    <ul>
      <li><router-link to="/">Home</router-link></li>
      <li><router-link to="/chronic-disease-prevention">만성질환 예방</router-link></li>
      <li><router-link to="/apart-farm">아파트 수직농장</router-link></li>
      <li><router-link to="/community-club">동네 커뮤니티 클럽</router-link></li>
      <!-- 필요에 따라 추가 메뉴 항목을 여기에 넣을 수 있습니다. -->
    </ul>
  </nav>
</template>

<style scoped>
.navigation-bar {
  padding: 10px;
  /* background: linear-gradient(to bottom, rgb(76, 175, 80), rgb(255, 255, 255));*/ /* 그라데이션 */
  background: rgb(76, 175, 80);
}

.navigation-bar ul {
  display: flex; /* flexbox 사용 */
  justify-content: center; /* 화면 중앙에 배치 */
  align-items: center; /* 세로 중앙 정렬 */
  list-style-type: none;
  margin: 0;
  padding: 0;
  gap: 130px; /* 메뉴 항목 사이 간격 */
}

.navigation-bar li {
  text-align: center;
}

.navigation-bar li a {
  display: block;
  color: black; /* 메뉴 항목 글자색 */
  text-align: center;
  padding: 14px 20px; /* 여백을 조금 더 넓게 */
  text-decoration: none;
  font-size: 1.5rem; /* 반응형 글자 크기 */
  font-weight: bold; /* 글자 굵기 */
}

.navigation-bar li a:hover {
    transform: scale(1.1); /* 약간 확대되는 애니메이션 */
    transition: transform 0.2s ease-in-out;
}

/* 반응형 디자인을 위해 추가 스타일 */
@media (max-width: 768px) {
  .navigation-bar li a {
    font-size: 1.2rem; /* 화면이 작을 때 글자 크기 조정 */
    padding: 12px 16px; /* 여백을 약간 줄임 */
  }
}
</style>
