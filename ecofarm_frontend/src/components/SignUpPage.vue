<template>
  <div class="title-bar">
    <img class="title-image" src="../assets/title2.png" alt="Home" @click="goHome"/>
  </div>
  <div class="signup-page">
    <div class="tabs">
      <button :class="{ active: activeTab === 'consumer' }" @click="activeTab = 'consumer'">소비자 회원가입</button>
      <button :class="{ active: activeTab === 'seller' }" @click="activeTab = 'seller'">판매자 회원가입</button>
    </div>

    <div class="form-container">
      <!-- 소비자 회원가입 폼 -->
      <form v-if="activeTab === 'consumer'" @submit.prevent="handleConsumerSignUp">
        <h2>소비자 회원가입</h2>
        <div class="form-group">
          <label for="name">이름:</label>
          <input type="text" id="name" v-model="name" required />
        </div>
        <div class="form-group">
          <label for="email">이메일:</label>
          <input type="email" id="email" v-model="email" required />
        </div>
        <div class="form-group">
          <label for="password">비밀번호:</label>
          <input type="password" id="password" v-model="password" required />
        </div>
        <div class="form-group">
          <label for="address">주소:</label>
          <input type="text" id="address" v-model="address" required />
        </div>
        <div class="form-group">
          <label for="phone">전화번호:</label>
          <input type="text" id="phone" v-model="phone" required />
        </div>
        <div class="form-group">
          <label for="age">나이:</label>
          <input type="text" id="age" v-model="age" required />
        </div>
        <div class="form-group">
          <label for="diseases">관심질환:</label>
          <div class="checkbox-group">
            <div v-for="disease in diseaseOptions" :key="disease" class="checkbox-item">
              <input type="checkbox" 
                    :value="disease" 
                    v-model="selectedDiseases" />
              <span class="checkbox-label">{{ disease }}</span>
            </div>
          </div>
        </div>
        <button type="submit">회원가입</button>
        <div v-if="consumerErrorMessage" class="error-message">{{ consumerErrorMessage }}</div>
      </form>

      <!-- 판매자 회원가입 폼 -->
      <form v-if="activeTab === 'seller'" @submit.prevent="handleSellerSignUp">
        <h2>판매자 회원가입</h2>
        <div class="form-group">
          <label for="name">이름:</label>
          <input type="text" id="name" v-model="name" required />
        </div>
        <div class="form-group">
          <label for="email">이메일:</label>
          <input type="email" id="email" v-model="email" required />
        </div>
        <div class="form-group">
          <label for="password">비밀번호:</label>
          <input type="password" id="password" v-model="password" required />
        </div>
        <div class="form-group">
          <label for="address">주소:</label>
          <input type="text" id="address" v-model="address" required />
        </div>
        <div class="form-group">
          <label for="phone">전화번호:</label>
          <input type="text" id="phone" v-model="phone" required />
        </div>
        <div class="form-group">
          <label for="apartment">아파트단지명:</label>
          <select id="apartment" v-model="selectedApartment" required>
            <option value="" disabled>아파트 단지를 선택하세요</option>
            <option v-for="apartment in apartments" :key="apartment.apartmentID" :value="apartment.apartmentID">
              {{ apartment.apartmentName }}
            </option>
          </select>
        </div>
        <button type="submit">회원가입</button>
        <div v-if="sellerErrorMessage" class="error-message">{{ sellerErrorMessage }}</div>
        <div v-if="apartmentErrorMessage" class="error-message">{{ apartmentErrorMessage }}</div>
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
      name: '',
      email: '',
      password: '',
      address: '',
      phone: '',
      age: '',
      diseases: '',
      selectedApartment: '',
      apartments: [], // 아파트 목록을 저장할 배열
      consumerErrorMessage: '', // 소비자 회원가입 오류 메시지
      sellerErrorMessage: '', // 판매자 회원가입 오류 메시지
      apartmentErrorMessage: '', // 아파트 리스트 오류 메시지
      selectedDiseases: [], 
      diseaseOptions: ['비만', '당뇨', '고혈압', '고지혈증', '심혈관질환', '암', '치매', '염증', '간질환'],
    };
  },
  methods: {
    goHome() {
      this.$router.push('/'); // 홈 페이지로 이동
    },
    async handleConsumerSignUp() {
      this.consumerErrorMessage = ''; // 초기화
      try {
        // 선택된 질병 배열을 문자열로 변환
        this.diseases = this.selectedDiseases.join(', ');

        const response = await fetch('http://localhost:8080/api/consumers/signup', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            consumerName: this.name,
            email: this.email,
            password: this.password,
            address: this.address,
            phoneNumber: this.phone,
            age: this.age,
            diseases: this.diseases
          })
        });

        if (response.ok) {
          // 회원가입 성공 처리
          alert('회원가입 성공!');
          this.$router.push('/login');
        } else if (response.status === 409) {
          this.consumerErrorMessage = '이미 가입된 이메일입니다.';
        } else {
          const errorData = await response.json();
          this.consumerErrorMessage = errorData.message || '회원가입에 실패했습니다.';
        }
      } catch (error) {
        console.error('회원가입 처리 중 오류 발생:', error);
        this.consumerErrorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    async handleSellerSignUp() {
      this.sellerErrorMessage = ''; // 초기화
      this.apartmentErrorMessage = ''; // 초기화
      try {
        // 판매자 회원가입 전 아파트 목록이 필요한 경우
        if (this.apartments.length === 0) {
          this.apartmentErrorMessage = '아파트 목록을 가져올 수 없습니다.';
          return;
        }

        const response = await fetch('http://localhost:8080/api/sellers/signup', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            sellerName: this.name,
            email: this.email,
            password: this.password,
            address: this.address,
            phoneNumber: this.phone,
            apartmentID: this.selectedApartment,
            approvedDate: ''
          })
        });

        if (response.ok) {
          // 회원가입 성공 처리
          alert('회원가입 요청 성공!');
          this.$router.push('/login');
        } else if (response.status === 409) {
          this.sellerErrorMessage = '이미 가입된 이메일입니다.';
        } else {
          const errorData = await response.json();
          this.sellerErrorMessage = errorData.message || '회원가입에 실패했습니다.';
        }
      } catch (error) {
        console.error('회원가입 처리 중 오류 발생:', error);
        this.sellerErrorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    async fetchApartments() {
      try {
        const response = await fetch('http://localhost:8080/api/apartments/list');
        if (response.ok) {
          this.apartments = await response.json();
        } else {
          console.error('Failed to fetch apartments');
          this.apartmentErrorMessage = '아파트 목록을 가져올 수 없습니다.';
        }
      } catch (error) {
        console.error('Error fetching apartments:', error);
        this.apartmentErrorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    resetForm() {
      // 폼 초기화
      this.name = '';
      this.email = '';
      this.password = '';
      this.address = '';
      this.phone = '';
      this.age = '';
      this.diseases = '';
      this.selectedApartment = '';
      this.consumerErrorMessage = '';
      this.sellerErrorMessage = '';
      this.apartmentErrorMessage = '';
    }
  },
  created() {
    // 컴포넌트가 생성될 때 아파트 목록을 가져옵니다.
    this.fetchApartments();
  }
};
</script>

<style scoped>
.title-bar {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
.title-image {
  cursor: pointer;
}
.signup-page {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #4CAF50;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  background-color: #F9FBE7;
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
  background-color: #A5D6A7;
  color: black;
  border-radius: 5px;
  font-size: 18px;
  transition: background-color 0.3s;
}

.tabs button.active {
  background-color: #388E3C;
  color: white;
}

.form-group {
  margin-bottom: 10px;
}

label {
  display: block;
  margin-bottom: 5px;
}

input, select {
  width: 90%;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #C8E6C9;
  border-radius: 5px;
  background-color: #ffffff;
}

button {
  padding: 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #388E3C;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap; /* 여러 줄로 감싸질 수 있도록 설정 */
}

.checkbox-item {
  display: flex;
  align-items: center; /* 체크박스와 라벨을 수직으로 정렬 */
  margin-right: 20px; /* 체크박스 간격 조정 */
}

.checkbox-label {
  margin-left: 8px; /* 체크박스와 라벨 간격 조정 */
  display: inline; /* 라벨이 줄 바꿈되지 않도록 설정 */
  white-space: nowrap; /* 라벨 텍스트가 줄 바꿈되지 않도록 설정 */
}


.error-message {
  color: red;
  margin-top: 10px;
}
</style>
