<template>
    <div class="seller-approve">
      <div v-if="errorMessage" class="error">
        <p>{{ errorMessage }}</p>
      </div>
      <h1>판매자 승인</h1>
      <!-- 승인 대기 판매자 섹션 -->
      <div v-if="pendingSellers.length > 0">
        <h3>승인 대기 판매자 : {{ pendingSellers.length }}명</h3>
        <table class="pending-seller-table">
          <thead>
            <tr>
              <th>등록번호</th>
              <th>이름</th>
              <th>이메일</th>
              <th>전화번호</th>
              <th>아파트 단지</th>
              <th class="approve">승인</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="seller in pendingSellers" :key="seller.sellerID">
              <td>판매자-{{ seller.sellerID }}</td>
              <td>{{ seller.sellerName }}</td>
              <td>{{ seller.email }}</td>
              <td>{{ seller.phoneNumber }}</td>
              <td>{{ seller.apartmentName }}</td>
              <td class="approve">
                <button @click="approveSeller(seller.sellerID)" class="approve-button">승인</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else>
        <p>승인 대기 중인 판매자가 없습니다.</p>
      </div>
    </div>
</template>
  
<script>
  export default {
    data() {
      return {
        pendingSellers: [],
        errorMessage: '',
      };
    },
    methods: {
      async fetchPendingSellers() {
        const token = sessionStorage.getItem('jwtToken');
        try {
          const response = await fetch('http://localhost:8080/api/sellers/pending', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          if (response.ok) {
            this.pendingSellers = await response.json();
            this.pendingSellers.sort((a, b) => b.sellerID - a.sellerID);
          } else if (response.status === 401) {
            this.$store.dispatch('logout');
            alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
            this.$router.push('/login');
          } else {
            const errorData = await response.json();
            this.errorMessage = errorData.message || '승인 대기 판매자 목록을 가져오는데 실패했습니다.';
          }
        } catch (error) {
          console.error('Failed to fetch pending sellers:', error);
          this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
        }
      },
      async approveSeller(sellerId) {
        const token = sessionStorage.getItem('jwtToken');
        try {
          const response = await fetch(`http://localhost:8080/api/sellers/approve/${sellerId}`, {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          });
          if (response.ok) {
            await response.json();
            this.fetchPendingSellers();
          } else if (response.status === 401) {
            this.$store.dispatch('logout');
            alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
            this.$router.push('/login');
          } else {
            const errorData = await response.json();
            this.errorMessage = errorData.message || '판매자 승인에 실패했습니다.';
          }
        } catch (error) {
          this.errorMessage = error.message;
        }
      },
    },
    created() {
      this.fetchPendingSellers();
    },
  };
</script>
  
<style scoped>
  .seller-approve {
    padding: 20px;
  }
  
  .pending-seller-table {
  width: 100%;
  table-layout: fixed; /* 고정 레이아웃 */
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px; 
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.pending-seller-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.pending-seller-table th,
.pending-seller-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.pending-seller-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.approve-button {
  background-color: #66bb6a; /* 버튼 배경색 */
  color: white; /* 글자색 */
  border: none;
  cursor: pointer;
  padding: 8px 16px; /* 버튼 내부 여백 */
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.approve-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

.pending-seller-table td.approve {
  text-align: center;
}

.pending-seller-table th.approve,
.pending-seller-table td.approve {
  width: 100px; /* 원하는 너비 설정 */
}
  
  .error {
    color: #d32f2f;
    margin-bottom: 20px;
  }
</style>
  