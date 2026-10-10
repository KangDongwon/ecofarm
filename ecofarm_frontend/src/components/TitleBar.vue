<template>
  <!-- 상단 오른쪽 메뉴와 타이틀 이미지를 감싸는 div -->
  <div class="header-section">
      <!-- 상단 오른쪽 메뉴 -->
      <div class="top-right-links">
        <span v-if="isLoggedIn" class="welcome-message">환영합니다. {{ name }}님</span>
        <a v-if="!isLoggedIn" href="/login">👤로그인</a>
        <a v-if="!isLoggedIn" href="/signup">📝회원가입</a>
        <a v-if="isLoggedIn" @click="navigateToProfile">📊대시보드</a>
        <a v-if="isLoggedIn" @click="logout">👋Logout</a>
      </div>
  </div>
  <div class="title-bar">
    <img class="title-image" src="../assets/title2.png" alt="Home" @click="goHome"/>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';

export default {
  methods: {
      goHome() {
          this.$router.push('/'); // 홈 페이지로 이동
      }
  },
  setup() {
    const router = useRouter();
    const store = useStore();

    const name = sessionStorage.getItem('name');
    const isLoggedIn = computed(() => !!store.state.token);
    const username = computed(() => store.state.username);
    const isAdmin = computed(() => store.state.userRole === 'ROLE_ADMIN'); // 사용자 역할이 ADMIN인지 확인

    const logout = () => {
      store.dispatch('logout')
        .then(() => {
          router.push('/');
        })
        .catch(error => {
          console.error('Logout error:', error);
        });
    };

    const navigateToProfile = () => {
      const role = store.state.userRole;
      if (role === 'ROLE_SELLER') {
        router.push('/seller');
      } else if (role === 'ROLE_CONSUMER') {
        router.push('/consumer');
      } else {
        router.push('/admin');
      }
    };

    return { isLoggedIn, username, isAdmin, logout, navigateToProfile, name };
  }
}
</script>

<style scoped>
.title-bar {
  background: linear-gradient(to top, rgb(76, 175, 80), rgb(255, 255, 255)); /* 그라데이션 */
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.title-image {
  cursor: pointer; /* 이미지에만 손 모양 커서 */
}

.top-right-links {
  position: absolute;
  top: 10px;
  right: 20px;
}

.top-right-links a {
  color: black; /* 글씨를 흰색으로 */
  padding: 3px 5px; /* 링크에 여백을 주어 버튼 느낌 */
  border-radius: 5px; /* 모서리를 둥글게 */
  text-decoration: none; /* 밑줄 제거 */
  font-weight: bold; /* 글씨 굵게 */
  font-size: 16px; /* 적당한 글씨 크기 */
  margin-left: 10px; /* 링크 간 여백 */
  transition: background-color 0.3s ease; /* 호버 시 부드러운 효과 */
  background-color: rgb(243, 227, 220);
}

.top-right-links a:hover {
  color: black; /* 글씨를 흰색 유지 */
  background-color: rgb(212, 198, 192);
  cursor: pointer;
}

.welcome-message {
  margin-right: 20px;
}
</style>
