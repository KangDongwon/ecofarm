<template>
  <div class="order-list">
    <h1>주문 진행 물품</h1>
    
    <!-- 로딩 중 메시지 표시 -->
    <div v-if="loading" class="loading-spinner-container">
      <div class="loading-spinner"></div> <!-- 로딩 스피너 -->
    </div>

    <!-- 에러 메시지 표시 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <!-- 테이블이 비어있을 경우 메시지 표시 -->
    <div v-if="!loading && filteredOrders.length === 0" class="no-orders-message">
      주문한 물품이 없습니다.
    </div>

    <!-- 주문 수량 표시 -->
    <div v-if="!loading && filteredOrders.length > 0" class="order-count">
      <h3>주문 진행 물품수 : {{ filteredOrders.length }}개</h3>
    </div>

    <!-- 주문 테이블 -->
    <table v-if="!loading && filteredOrders.length > 0" class="order-table">
      <thead>
        <tr>
          <th>주문번호</th>
          <th>물품명</th>
          <th>판매자명</th>
          <th>구매자명</th>
          <th>주문일자</th>
          <th>수량</th>
          <th>가격</th>
          <th class="status">주문상태</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(order, index) in filteredOrders" :key="order.orderID">
          <td>주문-{{ order.orderID }}</td>
          <td>{{ order.productName }}</td>
          <td>{{ order.sellerName }}</td>
          <td>{{ order.consumerName }}</td>
          <td>{{ order.orderDate }}</td>
          <td>{{ order.quantity }}</td>
          <td>{{ formatPrice(order.totalPrice) }}</td>
          <td class="status">{{ order.status }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  props: ['changeSection'],
  data() {
      return {
          orders: [], // 주문 목록
          errorMessage: null,
          loading: true // 로딩 상태 초기화
      };
  },
  computed: {
      filteredOrders() {
          return this.orders
              .filter(order => order.status !== '물품 전달 대기' && order.status !== '물품 전달 완료' && order.status !== '구매 취소')
              .sort((a, b) => a.orderID - b.orderID); // orderID로 오름차순 정렬
      }
  },
  methods: {
      async fetchOrders() {
          this.loading = true; // 로딩 시작
          try {
              // API 호출하여 주문 목록을 가져옵니다
              const response = await fetch(`http://localhost:8080/api/customerorders`, {
                  headers: { 'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}` }
              });
              if (response.ok) {
                  this.orders = await response.json();
                  console.log(this.orders);
              } else if (response.status === 401) {
                  this.$store.dispatch('logout');
                  alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
                  this.$router.push('/login');
              } else {
                  const errorData = await response.json();
                  this.errorMessage = errorData.message || '주문 리스트를 가져오는데 실패했습니다.';
              }
          } catch (error) {
              console.error('Failed to fetch orders:', error);
              this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
              this.orders = [];
          } finally {
              this.loading = false; // 로딩 완료
          }
      },
      async confirmTake(orderID, status) {
          try {
              const response = await fetch(`http://localhost:8080/api/customerorders/${orderID}`, {
                  method: 'PUT',
                  headers: {
                      'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}`,
                      'Content-Type': 'application/json'
                  },
                  body: JSON.stringify({ status: status }) // 상태를 포함
              });

              if (response.ok) {
                  alert('처리가 완료되었습니다.');
                  this.fetchOrders(); // 주문 리스트를 다시 불러오는 방법
              } else if (response.status === 401) {
                  this.$store.dispatch('logout');
                  alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
                  this.$router.push('/login');
              } else {
                  const errorData = await response.json();
                  alert(errorData.message || '처리 중 오류가 발생했습니다.');
              }
          } catch (error) {
              console.error('처리 중 오류 발생:', error);
              alert('서버와의 연결에 문제가 발생했습니다.');
          }
      },
      formatPrice(value) {
        return new Intl.NumberFormat('ko-KR').format(value); // 한국식 천 단위 콤마 처리
      },
  },
  created() {
      this.fetchOrders();
  }
};
</script>

<style scoped>
.order-list {
padding: 20px;
}

.order-table {
  width: 100%;
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px; 
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.order-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.order-table th,
.order-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.order-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.order-table td:nth-child(5), 
.order-table td:nth-child(6), 
.order-table td:nth-child(7) {
  text-align: right; /* 오른쪽 정렬 */
}

.no-orders-message {
margin-top: 20px;
font-size: 16px;
color: #333;
}

.status {
  width: 120px;
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
