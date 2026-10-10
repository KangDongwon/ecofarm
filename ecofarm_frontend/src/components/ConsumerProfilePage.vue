<template>
  <div class="profile-page">
    <h2>소비자 프로필</h2>

    <!-- 사용자 정보 표시 -->
    <div v-if="user">
      <div class="profile-info">
        <div class="form-group">
          <label for="username">사용자명:</label>
          <input v-model="user.consumerName" id="username" :readonly="!editMode" />
        </div>
        <div class="form-group">
          <label for="email">이메일:</label>
          <input v-model="user.email" id="email" :readonly="true" class="readonly" />
        </div>
        <div class="form-group">
          <label for="age">나이:</label>
          <input v-model="user.age" id="age" type="number" :readonly="!editMode" />
        </div>
        <div class="form-group">
          <label for="phone">전화번호:</label>
          <input v-model="user.phoneNumber" id="phone" :readonly="!editMode" />
        </div>
        <div class="form-group">
          <label for="address">주소:</label>
          <input v-model="user.address" id="address" :readonly="!editMode" />
        </div>
        <div class="form-group">
          <label for="diseases">관심질환:</label>
          <div v-if="!editMode">{{ user.diseases }}</div>
          <div v-else class="checkbox-group">
            <div v-for="disease in diseaseOptions" :key="disease" class="checkbox-item">
              <input type="checkbox" 
                    :value="disease" 
                    v-model="selectedDiseases" 
                    :checked="isChecked(disease)" />
              <span class="checkbox-label">{{ disease }}</span>
            </div>
          </div>
        </div>
        <div class="form-group" v-if="editMode">
          <label for="password">비밀번호:</label>
          <input type="password" v-model="password" id="password" />
        </div>
      </div>

      <!-- 수정 및 삭제 버튼 -->
      <div class="actions">
        <button v-if="!editMode" @click="editMode = true" class="button">수정</button>
        <button v-if="editMode" @click="updateUser" class="button">저장</button>
        <button v-if="editMode" @click="cancelEdit" class="button cancel-button">취소</button>
        <button v-if="!editMode" @click="deleteUser" class="button delete-button">계정 삭제</button>
      </div>
    </div>

    <!-- 에러 또는 성공 메시지 -->
    <div v-if="message" class="message">{{ message }}</div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';

export default {
  setup() {
    const store = useStore();
    const router = useRouter();
    const user = ref(null);
    const editMode = ref(false);
    const password = ref('');
    const message = ref('');
    const selectedDiseases = ref([]);
    const diseaseOptions = ['비만', '당뇨', '고혈압', '고지혈증', '심혈관질환', '암', '치매', '염증', '간질환']; // 선택 가능한 질병 목록

    // 사용자 정보 가져오기
    const fetchUser = async () => {
      try {
        const email = store.state.username; // Vuex에서 이메일 가져오기
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch(`http://localhost:8080/api/consumers/profile/${email}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          user.value = await response.json();
          // 질병 문자열을 배열로 변환하여 체크박스 선택 초기화
          selectedDiseases.value = user.value.diseases.split(',').map(disease => disease.trim());
        } else if (response.status === 401) {
          store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          router.push('/login');
        } else {
          const errorData = await response.json();
          message.value = errorData.message || '사용자 정보를 가져오는 데 오류가 발생했습니다.';
        }
      } catch (error) {
        console.error('사용자 정보 가져오기 오류:', error);
        message.value = '사용자 정보를 가져오는 데 오류가 발생했습니다.';
      }
    };

    // 사용자가 체크한 질병이 선택되었는지 확인하는 함수
    const isChecked = (disease) => {
      return selectedDiseases.value.includes(disease);
    };

    // 사용자 정보 업데이트
    const updateUser = async () => {
      try {
        const token = sessionStorage.getItem('jwtToken');
        const userData = {
          ...user.value,
          diseases: selectedDiseases.value.join(', '), // 체크된 질병을 문자열로 변환
          password: password.value // 비밀번호도 함께 전송
        };
        
        // 보내는 JSON 데이터를 로그로 출력
        //console.log('전송할 데이터:', JSON.stringify(userData, null, 2));

        const response = await fetch(`http://localhost:8080/api/consumers/update`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(userData)
        });

        if (response.ok) {
          editMode.value = false;
          fetchUser();
          //message.value = '프로필이 성공적으로 업데이트되었습니다.';
        } else if (response.status === 401) {
          store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          router.push('/login');
        } else {
          const errorData = await response.json();
          message.value = errorData.message || '사용자 정보를 업데이트하는 데 오류가 발생했습니다.';
        }
      } catch (error) {
        console.error('사용자 정보 업데이트 오류:', error);
        message.value = '사용자 정보를 업데이트하는 데 오류가 발생했습니다.';
      }
    };


    // 수정 모드 취소
    const cancelEdit = () => {
      fetchUser(); // 사용자 정보 새로 고침
      editMode.value = false;
      password.value = ''; // 비밀번호 필드 초기화
      selectedDiseases.value = user.value.diseases.split(',').map(disease => disease.trim()); // 체크박스 선택 초기화
    };

    // 사용자 계정 삭제
    const deleteUser = async () => {
      try {
        const email = store.state.username; // Vuex에서 이메일 가져오기
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch(`http://localhost:8080/api/consumers/delete/${email}`, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          store.dispatch('logout');
          router.push('/');
        } else if (response.status === 401) {
          store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          router.push('/login');
        } else {
          const errorData = await response.json();
          message.value = errorData.message || '계정 삭제에 실패했습니다.';
        }
      } catch (error) {
        console.error('계정 삭제 오류:', error);
        message.value = '계정 삭제에 오류가 발생했습니다.';
      }
    };

    // 컴포넌트 마운트 시 사용자 정보 가져오기
    onMounted(() => {
      fetchUser();
    });

    return { user, editMode, password, message, updateUser, cancelEdit, deleteUser, selectedDiseases, diseaseOptions, isChecked };
  }
};
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: rgb(235, 241, 235); /* 배경색을 관리자 페이지와 동일하게 설정 */
}

.profile-info {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #333; /* 라벨 색상 변경 */
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.form-group input:read-only {
  background-color: #f0f0f0; /* 비활성화된 입력 필드 배경색 */
  color: #666; /* 비활성화된 입력 필드 텍스트 색상 */
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

.actions {
  margin-top: 20px; /* 액션 버튼들 간의 위쪽 여백 */
}

.button {
  padding: 8px 16px;
  background-color: rgb(76, 175, 80); /* 관리자 페이지와 동일한 색상 */
  color: white;
  border: none;
  cursor: pointer;
  font-size: 14px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
  margin: 0 5px;
}

.button:hover {
  background-color: rgb(56, 133, 58); /* 밝은 초록색 */
}

.cancel-button {
  background-color: #6c757d;
}

.cancel-button:hover {
  background-color: #5a6268;
}

.delete-button {
  background-color: #dc3545;
}

.delete-button:hover {
  background-color: #c82333;
}

.message {
  margin-top: 20px;
  color: red;
}
</style>
