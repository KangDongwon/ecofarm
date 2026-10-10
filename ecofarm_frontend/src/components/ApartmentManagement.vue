<template>
  <div class="apartment-management">
    <button @click="showAddForm = !showAddForm" class="toggle-button">
      {{ showAddForm ? '취소' : '아파트 추가' }}
    </button>

    <div v-if="showAddForm" class="add-apartment-form">
      <h3>아파트 단지 추가</h3>
      <form @submit.prevent="submitForm">
        <div class="form-group">
          <label for="name">아파트 단지명:</label>
          <input v-model="newApartment.apartmentName" id="name" required />
        </div>
        <div class="form-group">
          <label for="address">주소:</label>
          <input v-model="newApartment.address" id="address" required />
        </div>
        <div class="form-group">
          <label for="startDate">계약 시작 날짜:</label>
          <input type="date" v-model="newApartment.contractStartDate" id="startDate" required />
        </div>
        <div class="form-group">
          <label for="endDate">계약 종료 날짜:</label>
          <input type="date" v-model="newApartment.contractEndDate" id="endDate" required />
        </div>
        <button type="submit" class="submit-button">저장</button>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      </form>
    </div>

    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
    
    <table v-else class="apartment-table">
      <thead>
        <tr>
          <th>아파트 단지명</th>
          <th>주소</th>
          <th>계약 시작 날짜</th>
          <th>계약 종료 날짜</th>
          <th class="edit">정보수정</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(apartment, index) in apartments" :key="apartment.apartmentID">
          <td>
            <template v-if="editingIndex === index">
              <input v-model="editableApartment.apartmentName" />
            </template>
            <template v-else>
              {{ apartment.apartmentName }}
            </template>
          </td>
          <td>
            <template v-if="editingIndex === index">
              <input v-model="editableApartment.address" />
            </template>
            <template v-else>
              {{ apartment.address }}
            </template>
          </td>
          <td class="contractStartDate">
            <template v-if="editingIndex === index">
              <input type="date" v-model="editableApartment.contractStartDate" />
            </template>
            <template v-else>
              {{ apartment.contractStartDate }}
            </template>
          </td>
          <td class="contractEndDate">
            <template v-if="editingIndex === index">
              <input type="date" v-model="editableApartment.contractEndDate" />
            </template>
            <template v-else>
              {{ apartment.contractEndDate }}
            </template>
          </td>
          <td class="edit">
            <template v-if="editingIndex === index">
              <button @click="saveApartment(apartment.apartmentID)" class="submit-button edit-button">저장</button>
              <button @click="cancelEdit" class="toggle-button edit-button">취소</button>
            </template>
            <template v-else>
              <button @click="editApartment(index, apartment)" class="toggle-button">수정</button>
            </template>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

  
<script>
export default {
  data() {
    return {
      showAddForm: false,
      newApartment: {
        apartmentName: '',
        address: '',
        contractStartDate: '',
        contractEndDate: ''
      },
      apartments: [],
      editingIndex: null, // 현재 편집 중인 행의 인덱스
      editableApartment: {}, // 편집 가능한 아파트 데이터
      errorMessage: ''
    };
  },
  methods: {
    async fetchApartments() {
      this.errorMessage = '';
      try {
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch('http://localhost:8080/api/apartments', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const data = await response.json();
          this.apartments = data;
        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '아파트 목록을 가져오는데 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to fetch apartments:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    async submitForm() {
      this.errorMessage = '';
      try {
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch('http://localhost:8080/api/apartments', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(this.newApartment)
        });

        if (response.ok) {
          this.showAddForm = false;
          this.newApartment = {
            apartmentName: '',
            address: '',
            contractStartDate: '',
            contractEndDate: ''
          };
          this.fetchApartments();
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '아파트 추가에 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to add apartment:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    editApartment(index, apartment) {
      this.editingIndex = index;
      this.editableApartment = { ...apartment };
    },
    cancelEdit() {
      this.editingIndex = null;
      this.editableApartment = {};
    },
    async saveApartment(id) {
      try {
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch('http://localhost:8080/api/apartments/update', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({ 
            apartmentID: id,
            apartmentName: this.editableApartment.apartmentName,
            address: this.editableApartment.address,
            contractStartDate: this.editableApartment.contractStartDate,
            contractEndDate: this.editableApartment.contractEndDate
          })
        });

        if (response.ok) {
          this.fetchApartments(); // 업데이트된 아파트 목록을 다시 가져옴
          this.editingIndex = null;
          this.editableApartment = {};
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '아파트 정보 수정에 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to update apartment:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    }
  },
  mounted() {
    this.fetchApartments();
  }
};
</script>

<style scoped>
.apartment-management {
  flex-direction: column;
}

.add-apartment-form {
  border-radius: 8px; /* 모서리 둥글게 */
  padding: 20px; /* 내부 여백 */
}

.add-apartment-form h3 {
  margin-bottom: 10px;
  color: black; /* 제목 색상 */
}

.add-apartment-form form {
  flex-direction: column;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: black;
}

.form-group input {
  width: 40%;
  padding: 10px; /* 패딩 */
  border: 1px solid #a5d6a7; /* 연한 초록색 테두리 */
  border-radius: 4px; /* 둥글게 */
  background-color: #fff; /* 흰색 배경 */
}

.submit-button {
  background-color: #66bb6a; /* 버튼 배경색 */
  color: white; /* 글자색 */
  border: none;
  cursor: pointer;
  border-radius: 4px;
  padding: 10px 20px; /* 버튼 내부 여백 */
  transition: background-color 0.3s ease;
}

.submit-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

.error-message {
  color: red;
  margin-top: 10px;
}

.apartment-table {
  width: 100%;
  table-layout: fixed; /* 고정 레이아웃 */
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px;
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.apartment-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.apartment-table th,
.apartment-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.apartment-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.apartment-table td input {
  width: 90%; /* 입력 필드 전체 너비 설정 */
  padding: 8px; /* 패딩 추가 */
  border: 1px solid #c8e6c9; /* 테두리 색상 */
  border-radius: 5px; /* 둥근 모서리 */
}

.toggle-button, .submit-button {
  display: inline-block; /* 내용에 맞게 크기 조정 */
  width: auto;
  padding: 8px 16px;
  background-color: #66bb6a; /* 버튼 배경색 */
  color: white;
  border: none;
  cursor: pointer;
  font-size: 14px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.toggle-button:hover, .submit-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

.apartment-table td.contractStartDate,
.apartment-table td.contractEndDate {
  text-align: right; /* 오른쪽 정렬 */
}

.apartment-table td.edit {
  text-align: center;
}

.apartment-table th.edit,
.apartment-table td.edit {
  width: 140px; /* 원하는 너비 설정 */
}

.edit-button {
  margin-left: 5px;
  margin-right: 5px; /* 버튼 사이에 10px의 마진 추가 */
}

</style>

  