<template>
  <div class="saleproduct-management">
    <h1>등록된 판매 물품</h1>

    <!-- 차트들을 나란히 표시하기 위한 div -->
    <div class="charts-container">
      <div class="pie-chart-wrapper">
        <PieChart v-if="productGroupData.length > 0" :productGroupData="productGroupData" />
      </div>
      <div class="pie-chart-wrapper">
        <PieChartApart v-if="apartGroupData.length > 0" :apartGroupData="apartGroupData" />
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

    <!-- 총 물품 개수 표시 -->
    <div v-if="!loading && filteredProducts.length > 0" class="total-products-message">
      <h3>물품 수 : {{ filteredProducts.length }}개</h3>
    </div>

    <!-- 테이블이 비어있을 경우 메시지 표시 -->
    <div v-if="!loading && filteredProducts.length === 0" class="no-products-message">
      승인된 판매 물품이 없습니다.
    </div>

    <!-- 판매 물품 테이블 -->
    <table v-else-if="!loading" class="product-table">
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
        </tr>
      </thead>
      <tbody>
        <tr v-for="(product, index) in filteredProducts" :key="product.saleProductID">
          <td>판매-{{ product.saleProductID }}</td>
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
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import PieChartApart from './SaleProductApartPieChartAdmin.vue';
import PieChart from './SaleProductPieChartAdmin.vue';

export default {
  components: {
    PieChartApart,
    PieChart,
  },
  data() {
    return {
      products: [], // 제품 목록
      productGroupData: [], // productName 별 그룹 데이터
      apartGroupData: [], // apartmentName 별 그룹 데이터
      errorMessage: '',
      loading: true, // 로딩 상태 추가
    };
  },
  computed: {
    filteredProducts() {
      const currentDate = new Date(); // 현재 날짜
      // approvalStatus가 '승인'이고 availableUntilDate가 현재 날짜보다 이후인 제품만 필터링
      return this.products.filter(product => 
        product.approvalStatus === '승인' && 
        new Date(product.availableUntilDate) >= currentDate
      );
    }
  },
  methods: {
    // productName 별로 그룹화하는 함수
    groupByProductName() {
      const group = {};
      this.filteredProducts.forEach((product) => {
        const name = product.productName;
        if (!group[name]) {
          group[name] = 0;
        }
        group[name] += 1; // 같은 productName을 가진 제품 개수를 세기
      });
      this.productGroupData = Object.keys(group).map((key) => ({
        name: key,
        value: group[key],
      }));
    },
    formatPrice(value) {
      return new Intl.NumberFormat('ko-KR').format(value); // 한국식 천 단위 콤마 처리
    },
    // apartmentName 별로 그룹화하는 함수
    groupByApartmentName() {
      const group = {};
      this.filteredProducts.forEach((product) => {
        const name = product.apartmentName;
        if (!group[name]) {
          group[name] = 0;
        }
        group[name] += 1; // 같은 apartmentName을 가진 제품 개수를 세기
      });
      this.apartGroupData = Object.keys(group).map((key) => ({
        name: key,
        value: group[key],
      }));
    },

    async fetchProducts() {
      try {
        this.loading = true;
        // API 호출하여 제품 목록을 가져옵니다
        const response = await fetch('http://localhost:8080/api/saleproducts', {
          headers: { 'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}` }
        });
        if (response.ok) {
          this.products = await response.json();
          this.groupByProductName(); // 제품 목록을 가져온 후 productName 별 그룹화
          this.groupByApartmentName();
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
        this.loading = false; // 로딩 완료 후 로딩 상태 해제
      }
    }
  },
  created() {
    this.fetchProducts();
  }
};
</script>

<style scoped>
.saleproduct-management {
  padding: 20px;
}

.charts-container {
  display: flex;
  justify-content: center; /* 차트를 양쪽에 배치 */
  flex-wrap: wrap; /* 화면이 작아질 경우 차트를 줄 바꿈 */
  margin-bottom: 20px;
}

.pie-chart-wrapper {
  flex: 1; /* flex 속성을 사용하여 비율에 맞게 차트 크기 조정 */
  min-width: 300px; /* 차트의 최소 너비 설정 */
  max-width: 800px; /* 최대 너비 설정 */
  margin: 0 40px; /* 차트 사이에 여백 추가 */
}

.loading-message {
  font-size: 18px;
  color: #666;
}

.product-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.product-table th, .product-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.product-table th {
  background-color: #e8f5e9; /* 연한 녹색 배경 */
}

.product-table a {
  color: #388e3c; /* 초록색 링크 */
  text-decoration: none;
}

.product-table a:hover {
  text-decoration: underline;
}

.no-products-message {
  margin-top: 20px;
  font-size: 16px;
  color: #333;
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
.product-table td:nth-child(7), 
.product-table td:nth-child(10) {
  text-align: right; /* 오른쪽 정렬 */
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
