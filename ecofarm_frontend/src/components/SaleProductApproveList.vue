<template>
    <div class="saleproduct-approvelist">
      <h1>판매 승인 대기 물품</h1>
      <!-- 에러 메시지 표시 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>

      <!-- 총 물품 개수 표시 -->
      <div v-if="!loading && filteredProducts.length > 0" class="total-products-message">
        <h3>대기 물품수: {{ filteredProducts.length }}개</h3>
      </div>

      <!-- 테이블이 비어있을 경우 메시지 표시 -->
      <div v-if="filteredProducts.length === 0" class="no-products-message">
        승인 대기 중인 판매 물품이 없습니다.
      </div>
      
      <!-- 판매 물품 테이블 -->
      <table v-else class="product-table">
        <thead>
          <tr>
            <th>등록번호</th>
            <th>아파트명</th>
            <th>판매 물품명</th>
            <th>판매자명</th>
            <th>구매기한일자</th>
            <th>수량</th>
            <th>가격</th>
            <th>유튜브 링크</th>            
            <th>승인 상태</th>
            <th>승인일자</th>
            <th class="approve">판매승인</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(product, index) in filteredProducts" :key="product.saleProductID">
            <td>판매대기-{{ product.saleProductID }}</td>
            <td>{{ product.apartmentName }}</td>
            <td>{{ product.productName }}</td>
            <td>{{ product.sellerName }}</td>
            <td>{{ product.availableUntilDate }}</td>
            <td>{{ product.quantity }}</td>
            <td>{{ formatPrice(product.price) }}</td>
            <td>
              <a v-if="product.youTubeLink" :href="product.youTubeLink" target="_blank">동영상 보기</a>
              <span v-else>링크 없음</span>
            </td>
            <td>{{ product.approvalStatus }}</td>
            <td>{{ product.approvalDate }}</td>
            <td class="approve">
              <button @click="approveProduct(product.saleProductID)" class="toggle-button">승인</button>
              <button @click="rejectProduct(product.saleProductID)" class="toggle-button reject-button">거절</button>
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
        products: [], // 제품 목록
        loading: true,
        errorMessage: ''
      };
    },
    computed: {
      filteredProducts() {
        return this.products.filter(product => product.approvalStatus === '대기중');
      }
    },
    methods: {
      async fetchProducts() {
        try {
          this.loading = true;
          // API 호출하여 제품 목록을 가져옵니다
          const response = await fetch('http://localhost:8080/api/saleproducts', {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}` }
          });
          if (response.ok) {
            this.products = await response.json();
          } else if (response.status === 401) {
            this.$store.dispatch('logout');
            alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
            this.$router.push('/login');
          } else {
            const errorData = await response.json();
            this.errorMessage = errorData.message || '판매 물품 목록을 가져오는데 실패했습니다.';
          }
        } catch (error) {
          console.error('Failed to fetch saleproducts:', error);
          this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
          this.products = [];
        } finally {
          this.loading = false;
        }
      },
      async approveProduct(saleProductID) {
        try {
          // 제품 승인 처리
          const token = sessionStorage.getItem('jwtToken');
          const response = await fetch(`http://localhost:8080/api/saleproducts/approve/${saleProductID}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${token}`
            }
          });
  
          if (response.ok) {
            // 승인 성공 후 제품 목록 새로고침
            this.fetchProducts();
          } else if (response.status === 401) {
            this.$store.dispatch('logout');
            alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
            this.$router.push('/login');
          } else {
            const errorData = await response.json();
            this.errorMessage = errorData.message || '판매 물품 승인이 실패했습니다.';
          }
        } catch (error) {
          console.error('Failed to approve saleproduct:', error);
          this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
          this.products = [];
        }
      },
      async rejectProduct(saleProductID) {
        try {
          // 제품 거절 처리
          const token = sessionStorage.getItem('jwtToken');
          const response = await fetch(`http://localhost:8080/api/saleproducts/reject/${saleProductID}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${token}`
            }
          });
  
          if (response.ok) {
            // 승인 성공 후 제품 목록 새로고침
            this.fetchProducts();
          } else if (response.status === 401) {
            this.$store.dispatch('logout');
            alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
            this.$router.push('/login');
          } else {
            const errorData = await response.json();
            this.errorMessage = errorData.message || '판매 물품 승인이 실패했습니다.';
          }
        } catch (error) {
          console.error('Failed to reject saleproduct:', error);
          this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
          this.products = [];
        }
      },
      formatPrice(value) {
        return new Intl.NumberFormat('ko-KR').format(value); // 한국식 천 단위 콤마 처리
      },
    },
    created() {
      this.fetchProducts();
    }
  };
</script>

<style scoped>
.saleproduct-approvelist {
  padding: 20px;
}

.loading-message {
  font-size: 18px;
  color: #666;
}

.product-table {
  width: 100%;
  table-layout: fixed; /* 고정 레이아웃 */
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px; 
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.product-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.product-table th,
.product-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.product-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.product-table td:nth-child(5), 
.product-table td:nth-child(6), 
.product-table td:nth-child(7) {
  text-align: right; /* 오른쪽 정렬 */
}

.approve {
  text-align: center;
  width: 120px;
}

.toggle-button {
  background-color: #4CAF50; /* 승인 버튼 배경 */
  color: white; /* 버튼 텍스트 색상 */
  border: none; /* 테두리 제거 */
  padding: 5px 10px; /* 버튼 패딩 */
  cursor: pointer; /* 커서 변경 */
  border-radius: 5px; /* 둥근 모서리 */
  margin-right: 5px; /* 버튼 사이 여백 */
}

.toggle-button:hover {
  background-color: #45a049; /* 호버 시 색상 변경 */
}

.reject-button {
  background-color: #c72216; /* 거절 버튼 배경 */
}

.reject-button:hover {
  background-color: #aa1b11; /* 호버 시 색상 변경 */
}

.loading-spinner-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px; /* 스피너가 위치할 영역의 높이 */
}

.loading-spinner {
  border: 8px solid #f3f3f3; /* 바깥 테두리 */
  border-top: 8px solid #3498db; /* 스피너 상단 부분 */
  border-radius: 50%;
  width: 60px;
  height: 60px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>