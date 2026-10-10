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
            <th>판매자명</th>
            <th>주문일자</th>
            <th>수량</th>
            <th>가격</th>
            <th>주문상태</th>
            <th>확인</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(order, index) in orders" :key="order.orderID">
            <td>{{ order.productName }}</td>
            <td>{{ order.sellerName }}</td>
            <td class="orderDate">{{ order.orderDate }}</td>
            <td class="quantity">{{ order.quantity }}</td>
            <td class="totalPrice">{{ formatPrice(order.totalPrice) }}</td>
            <td class="status">{{ order.status === '물품 전달 완료' ? '물품 인수 완료' : order.status }}</td>
            <td class="confirm">
                <button v-if="order.status === '입금 대기'" @click="payForOrder(order.orderID)" class="toggle-button">입금하기</button>
                <button v-if="order.status === '물품 전달 완료'" @click="goReiview(order)" class="toggle-button">리뷰쓰기</button>
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
      async fetchOrders() {
        try {
          const email = this.$store.state.username;
          // API 호출하여 주문 목록을 가져옵니다
          const response = await fetch(`http://localhost:8080/api/customerorders/consumerorders?email=${encodeURIComponent(email)}`, {
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
      async payForOrder(orderID) {
        try {
            const response = await fetch(`http://localhost:8080/api/customerorders/${orderID}`, {
                method: 'PUT',
                headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}`,
                'Content-Type': 'application/json'
                },
                body: JSON.stringify({ status: '입금 완료' }) // 상태를 포함
            });
            
            if (response.ok) {
                alert('입금이 완료되었습니다.');
                this.fetchOrders(); // 주문 리스트를 다시 불러오는 방법
            } else if (response.status === 401) {
                this.$store.dispatch('logout');
                alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
                this.$router.push('/login');
            } else {
                const errorData = await response.json();
                alert(errorData.message || '입금 처리 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('입금 요청 중 오류 발생:', error);
            alert('서버와의 연결에 문제가 발생했습니다.');
        }
      },
      goReiview(order) {
        this.changeSection('reviewWrite', null, order);
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
  background-color: #e8f5e9; /* 연한 녹색 배경 */
  border-radius: 10px; /* 둥근 모서리 */
}

.order-table {
  width: 100%;
  table-layout: fixed;
  border-collapse: collapse;
  margin-top: 20px;
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과가 제대로 보이도록 설정 */
}

.order-table th {
  background-color: #4CAF50; /* 진한 녹색 배경색 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.order-table th, .order-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.order-table td {
  background-color: #f1f8e9; /* 각 셀의 배경색을 연한 초록색으로 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.order-table td.quantity,
.order-table td.totalPrice,
.order-table td.orderDate {
  text-align: right; /* 오른쪽 정렬 */
}

.order-table td.status {
  text-align: center; /* 중앙 정렬 */
}

.order-table td.confirm {
  text-align: center; /* 버튼을 가운데 정렬 */
  width: 50px; /* 원하는 고정 너비 설정 */
}

.no-orders-message {
  margin-top: 20px;
  font-size: 16px;
  color: #388e3c; /* 어두운 녹색 */
}

.toggle-button {
  padding: 8px 16px;
  background-color: #66bb6a; /* 버튼 배경색을 채소 느낌으로 */
  color: white;
  border: none;
  cursor: pointer;
  font-size: 14px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.toggle-button:hover {
  background-color: #388e3c; /* 버튼 호버 색상 */
}

.order-table {
  table-layout: fixed; /* 테이블 레이아웃을 고정으로 설정 */
  width: 100%; /* 테이블 전체 너비 설정 */
}

.order-table th,
.order-table td {
  overflow: hidden; /* 콘텐츠가 셀을 벗어나지 않도록 */
  text-overflow: ellipsis; /* 내용이 길 경우 말줄임표 */
}
</style>
  