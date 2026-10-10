<template>
    <div class="order-complete">
      <h1>주문 처리 완료</h1>
      <!-- 연도 선택 드롭다운 -->
      <div v-if="availableYears.length > 0" class="year-selection">
        <label for="year-select">연도선택 : </label>
        <select id="year-select" v-model="selectedYear">
          <option v-for="year in availableYears" :key="year" :value="year">
            {{ year }}
          </option>
        </select>
      </div>
      <!-- 차트들을 나란히 표시하기 위한 div -->
      <div class="charts-container">
        <div class="bar-chart-wrapper">
          <BarChart v-if="monthlyData.length > 0" :monthlyData="monthlyData" :selectedYear="selectedYear" />
        </div>
        <div class="pie-chart-wrapper">
          <AdminOrderCompletePieChartCancel v-if="cancelGroupData.length > 0" :cancelGroupData="cancelGroupData" />
        </div>
      </div>

      <!-- 로딩 중 메시지 표시 -->
      <div v-if="loading" class="loading-spinner-container">
        <div class="loading-spinner"></div> <!-- 로딩 스피너 -->
      </div>

      <!-- 에러 메시지 표시 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      
      <!-- 주문 수량 표시 -->
      <div v-if="!loading && filteredOrders.length > 0" class="order-count">
        <h3>주문 처리 물품 수 : {{ filteredOrders.length }}개</h3>
      </div>

      <!-- 테이블이 비어있을 경우 메시지 표시 -->
      <div v-if="!loading && filteredOrders.length === 0" class="no-orders-message">
        구매 리스트를 가져오는데 실패하였습니다.
      </div>

      <!-- 주문 테이블 -->
      <table v-if="!loading && filteredOrders.length > 0" class="order-table">
        <thead>
          <tr>
            <th>주문번호</th>
            <th>아파트명</th>
            <th>물품명</th>
            <th>판매자명</th>
            <th>구매자명</th>
            <th>주문일자</th>
            <th>수량</th>
            <th>가격</th>
            <th>주문상태</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(order, index) in filteredOrders" :key="order.orderID">
            <td>주문-{{ order.orderID }}</td>
            <td>{{ order.apartmentName }}</td>
            <td>{{ order.productName }}</td>
            <td>{{ order.sellerName }}</td>
            <td>{{ order.consumerName }}</td>
            <td>{{ order.orderDate }}</td>
            <td>{{ order.quantity }}</td>
            <td>{{ formatPrice(order.totalPrice) }}</td>
            <td>{{ order.status }}</td>
          </tr>
        </tbody>
      </table>
    </div>
</template>
  
<script>
import BarChart from './AdminOrderCompleteBarChart.vue';
import AdminOrderCompletePieChartCancel from './AdminOrderCompletePieChartCancel.vue';

  export default {
    props: ['changeSection'],
    components: {
      BarChart,
      AdminOrderCompletePieChartCancel,
    },
    data() {
      return {
        orders: [], // 주문 목록
        errorMessage: null,
        monthlyData: [], // 월별 데이터
        selectedYear: '',
        availableYears: [], // 선택 가능한 연도 목록
        apartGroupData: [],
        cancelGroupData: [],
        productGroupData: [],
        loading: true // 로딩 상태 추가
      };
    },
    computed: {
      filteredOrders() {
        return this.orders
            .filter(order => order.status === '물품 전달 완료' || order.status === '구매 취소')
            .sort((a, b) => a.orderID - b.orderID); // orderID로 오름차순 정렬
      }
    },
    methods: {
      async fetchOrders() {
        try {
          this.loading = true; // 로딩 시작
          // API 호출하여 주문 목록을 가져옵니다
          const response = await fetch(`http://localhost:8080/api/customerorders`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}` }
          });
          if (response.ok) {
            this.orders = await response.json();
            //console.log(this.orders);
            // 연도 목록 생성
            this.availableYears = [...new Set(this.orders.map(order => 
              String(new Date(order.orderDate).getFullYear()) // 연도를 문자열로 변환
            ))].sort((a, b) => b - a);

            // 데이터를 가져온 후 기본 연도 선택
            this.selectedYear = this.availableYears[0]; // 기본 연도 선택

            // 데이터 가공
            this.generateMonthlyData();
            this.groupByApartmentName();
            this.groupByCancel();
            this.groupByProdcut();
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
            this.loading = false;
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
      generateMonthlyData() {
        const monthlyData = {};

        this.filteredOrders.forEach(orders => {
          if (orders.status == '물품 전달 완료') {
            const month = new Date(orders.orderDate)
              .toISOString()
              .split('T')[0]
              .slice(0, 7); // 'yyyy-MM' 형식으로 변환

            if (!monthlyData[month]) {
              monthlyData[month] = 0;
            }
            monthlyData[month]++; // 해당 월의 카운트 증가
          }
        });

        // Object.entries로 데이터를 배열로 변환한 후 month를 기준으로 정렬
        this.monthlyData = Object.entries(monthlyData)
          .sort(([monthA], [monthB]) => new Date(`${monthA}-01`) - new Date(`${monthB}-01`)) // 월별 정렬
          .map(([month, count]) => ({
            month,
            count
          }));
      },
      // apartmentName 별로 그룹화하는 함수
      groupByApartmentName() {
        const group = {};
        this.filteredOrders.forEach((order) => {
          if (order.status == '물품 전달 완료') {
            const name = order.apartmentName;
            if (!group[name]) {
              group[name] = 0;
            }
            group[name] += 1; // 같은 apartmentName을 가진 제품 개수를 세기
          }
        });
        
        this.apartGroupData = Object.keys(group).map((key) => ({
          name: key,
          value: group[key],
        }));
      },
      // 구매 / 취소 별로 그룹화하는 함수
      groupByCancel() {
        const group = {};
        this.filteredOrders.forEach((order) => {
          const name = order.status;
          if (!group[name]) {
            group[name] = 0;
          }
          group[name] += 1;
        });
        
        this.cancelGroupData = Object.keys(group).map((key) => ({
          name: key,
          value: group[key],
        }));
      },
      // 품목 별로 그룹화하는 함수
      groupByProdcut() {
        const group = {};
        this.filteredOrders.forEach((order) => {
          if (order.status == '물품 전달 완료') {
            const name = order.productName;
            if (!group[name]) {
              group[name] = 0;
            }
            group[name] += 1; // 같은 apartmentName을 가진 제품 개수를 세기
          }
        });
        
        this.productGroupData = Object.keys(group).map((key) => ({
          name: key,
          value: group[key],
        }));
      },
      formatPrice(value) {
        return new Intl.NumberFormat('ko-KR').format(value); // 한국식 천 단위 콤마 처리
      },
    },
    watch: {
      selectedYear(newYear) {
        this.generateMonthlyData(); // 연도 변경 시 월별 데이터 재생성
      }
    },
    created() {
      this.fetchOrders();
    }
  };
</script>
  
<style scoped>
.order-complete {
  padding: 20px;
}

.charts-container {
  display: flex;
  justify-content: space-around; 
  flex-wrap: wrap; /* 화면이 작아질 경우 차트를 줄 바꿈 */
  margin-bottom: 20px;
}

.bar-chart-wrapper,
.pie-chart-wrapper {
  flex: 1; /* flex 속성을 사용하여 비율에 맞게 차트 크기 조정 */
  min-width: 300px; /* 차트의 최소 너비 설정 */
  max-width: 800px; /* 최대 너비 설정 */
  margin: 0 40px; /* 차트 사이에 여백 추가 */
}

/* 연도 선택 드롭다운 스타일 */
.year-selection {
  margin-bottom: 20px;
}

.year-selection label {
  font-weight: bold;
  margin-right: 10px;
}

.year-selection select {
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f9f9f9;
}

.year-selection select:focus {
  outline: none;
  border-color: #4caf50; /* 초록색 테두리 */
  box-shadow: 0 0 5px rgba(76, 175, 80, 0.5); /* 초록색 그림자 */
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

.order-table td:nth-child(8), 
.order-table td:nth-child(6), 
.order-table td:nth-child(7) {
  text-align: right; /* 오른쪽 정렬 */
}

.no-orders-message {
  margin-top: 20px;
  font-size: 16px;
  color: #333;
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

  