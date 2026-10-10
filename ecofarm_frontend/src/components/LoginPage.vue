<template>
  <div class="title-bar">
    <img class="title-image" src="../assets/title2.png" alt="Home" @click="goHome"/>
  </div>
    <div class="login-page">
      <!-- 탭 메뉴 -->
      <div class="tabs">
        <button :class="{ active: activeTab === 'consumer' }" @click="activeTab = 'consumer'">소비자 로그인</button>
        <button :class="{ active: activeTab === 'seller' }" @click="activeTab = 'seller'">판매자 로그인</button>
      </div>
      
      <!-- 로그인 폼 -->
      <div class="login-form">
        <form v-if="activeTab === 'consumer'" @submit.prevent="submitLogin('consumer')">
          <h2>소비자 로그인</h2>
          <input type="email" v-model="consumerEmail" placeholder="Email" required />
          <input type="password" v-model="consumerPassword" placeholder="Password" required />
          <button type="submit">Login</button>
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        </form>
        
        <form v-if="activeTab === 'seller'" @submit.prevent="submitLogin('seller')">
          <h2>판매자 로그인</h2>
          <input type="email" v-model="sellerEmail" placeholder="Email" required />
          <input type="password" v-model="sellerPassword" placeholder="Password" required />
          <button type="submit">Login</button>
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        </form>
      </div>
    </div>
</template>
  
<script>
  import TitleBar from './TitleBar.vue';
  
  export default {
    components: {
      TitleBar
    },
    data() {
      return {
        activeTab: 'consumer',
        consumerEmail: '',
        consumerPassword: '',
        sellerEmail: '',
        sellerPassword: ''
      };
    },
    computed: {
      errorMessage() {
        return this.$store.state.error; // Vuex 상태에서 오류 메시지 가져오기
      }
    },
    methods: {
      goHome() {
        this.$router.push('/'); // 홈 페이지로 이동
      },
      async submitLogin(userType) {
        const email = userType === 'consumer' ? this.consumerEmail : this.sellerEmail;
        const password = userType === 'consumer' ? this.consumerPassword : this.sellerPassword;
        
        try {
          const isSuccess = await this.$store.dispatch('login', { email, password });
          if (isSuccess) {
            const role = this.$store.state.userRole;

            if (role === 'ROLE_SELLER') {
              this.$router.push('/');
            } else if (role === 'ROLE_CONSUMER') {
              this.$router.push('/');
            } else {
              this.$router.push('/');
            }
          }
        } catch (error) {
          console.error('Login failed:', error);
        }
      }
    }
  }
 </script>
  
<style scoped>
.title-bar {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
.title-image {
  cursor: pointer; /* 이미지에만 손 모양 커서 */
}
.login-page {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #4CAF50; /* 채소 느낌의 녹색 테두리 */
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  background-color: #F9FBE7; /* 부드러운 배경색 */
}

.tabs {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.tabs button {
  padding: 10px 20px;
  border: none;
  cursor: pointer;
  background-color: #A5D6A7; /* 연한 녹색 배경 */
  color: black; /* 글씨 색상 */
  border-radius: 5px; /* 버튼 모서리 둥글게 */
  font-size: 18px; /* 글자 크기 증가 */
  transition: background-color 0.3s; /* 부드러운 호버 효과 */
}

.tabs button.active {
  background-color: #388E3C; /* 활성화된 버튼의 짙은 녹색 */
  color: white; /* 글씨를 흰색으로 */
}

.login-form form {
  display: flex;
  flex-direction: column;
}

.login-form input {
  margin-bottom: 10px;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #C8E6C9; /* 연한 녹색 테두리 */
  border-radius: 5px; /* 모서리 둥글게 */
  background-color: #ffffff; /* 흰색 배경 */
}

.login-form button {
  padding: 10px;
  background-color: #4CAF50; /* 신선한 녹색 */
  color: white;
  border: none;
  border-radius: 5px; /* 모서리 둥글게 */
  cursor: pointer;
  font-size: 18px; /* 글자 크기 증가 */
  transition: background-color 0.3s; /* 부드러운 호버 효과 */
}

.login-form button:hover {
  background-color: #388E3C; /* 호버 시 어두운 녹색 */
}

.error-message {
  color: red;
  margin-top: 10px;
}
</style>
  