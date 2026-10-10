<template>
  <div class="order-list">
    <h2>주문 리스트</h2>
    <!-- 에러 메시지 표시 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <!-- 테이블이 비어있을 경우 메시지 표시 -->
    <div v-if="orders.length === 0" class="no-orders-message">
      주문한 물품이 없습니다.
    </div>

    <!-- 주문 테이블 -->
    <table v-else class="order-table">
      <thead>
        <tr>
          <th>물품명</th>
          <th>구매자명</th>
          <th>주문일자</th>
          <th>수량</th>
          <th>가격</th>
          <th>주문상태</th>
          <th class="confirm">확인</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(order, index) in orders" :key="order.orderID">
          <td>{{ order.productName }}</td>
          <td>{{ order.consumerName }}</td>
          <td>{{ order.orderDate }}</td>
          <td>{{ order.quantity }}</td>
          <td>{{ formatPrice(order.totalPrice) }}</td>
          <td>{{ order.status }}</td>
          <td class="confirm">
            <button v-if="order.status === '입금 완료'" @click="confirmPayment(order.orderID)" class="toggle-button">입금확인</button>
          </td>
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
        errorMessage: null
      };
    },
    methods: {
      formatPrice(value) {
        return new Intl.NumberFormat('ko-KR').format(value); // 한국식 천 단위 콤마 처리
      },
      async fetchOrders() {
        try {
          const email = this.$store.state.username;
          // API 호출하여 주문 목록을 가져옵니다
          const response = await fetch(`http://localhost:8080/api/customerorders/sellerorders?email=${encodeURIComponent(email)}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}` }
          });
          if (response.ok) {
            this.orders = await response.json();
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
        }
      },
      async confirmPayment(orderID) {
        try {
            const response = await fetch(`http://localhost:8080/api/customerorders/${orderID}`, {
                method: 'PUT',
                headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: '물품 전달 대기' }) // 상태를 포함
            });
            
            if (response.ok) {
                alert('입금이 확인되었습니다.');
                this.fetchOrders(); // 주문 리스트를 다시 불러오는 방법
            } else if (response.status === 401) {
                this.$store.dispatch('logout');
                alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
                this.$router.push('/login');
            } else {
                const errorData = await response.json();
                alert(errorData.message || '입금 확인 처리 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('입금 확인 중 오류 발생:', error);
            alert('서버와의 연결에 문제가 발생했습니다.');
        }
      }
    },
    created() {
      this.fetchOrders();
    }
  };
  </script>
  
<style scoped>
.order-list {
  flex-direction: column;
}

.order-list h2 {
  color: black; /* 제목 색상 */
}

.error-message {
  color: red;
  margin-top: 10px;
}

.no-orders-message {
  text-align: center; /* 가운데 정렬 */
  font-size: 16px; /* 글자 크기 */
  color: #666; /* 회색 글씨 */
}

.order-table {
  width: 100%;
  table-layout: fixed; /* 고정 레이아웃 */
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px;
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.order-table th {
  background-color: #4caf50; /* 진한 녹색 배경 */
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

.toggle-button {
  margin-left: 5px;
  margin-right: 5px;
  padding: 8px 16px;
  background-color: #66bb6a; /* 버튼 배경색 */
  color: white;
  border: none;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.toggle-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

.confirm {
  text-align: center; /* 가운데 정렬 */
  width: 100px;
}

.order-table td:nth-child(3), /* 주문일자 */
.order-table td:nth-child(4), /* 수량 */
.order-table td:nth-child(5) { /* 가격 */
  text-align: right; /* 오른쪽 정렬 */
}

</style>