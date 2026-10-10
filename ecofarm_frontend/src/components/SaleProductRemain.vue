<template>
    <div class="saleproduct-remain">
      <h1>판매 재고 물품</h1>

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
          <PieChart v-if="productGroupData.length > 0" :productGroupData="productGroupData" />
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
        판매 재고 물품이 없습니다.
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
            <td>재고-{{ product.saleProductID }}</td>
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
import BarChart from './RemainProductBarChart.vue';
import PieChart from './RemainProductPieChart.vue';

  export default {
    components: {
      BarChart,
      PieChart,
    },
    data() {
      return {
        products: [], // 제품 목록
        monthlyData: [], // 월별 데이터
        productGroupData: [], // 물품 그룹 데이터
        selectedYear: '', // 기본값: 현재 연도
        availableYears: [], // 선택 가능한 연도 목록
        errorMessage: '',
        loading: true // 로딩 상태 추가
      };
    },
    computed: {
      filteredProducts() {
        const currentDate = new Date(); // 현재 날짜
        // approvalStatus가 '승인'이고 availableUntilDate가 현재 날짜보다 이후인 제품만 필터링
        return this.products.filter(product => 
          product.approvalStatus === '승인' && 
          new Date(product.availableUntilDate) < currentDate &&
          product.quantity > 0
        );
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
            // 연도 목록 생성
            this.availableYears = [...new Set(this.products.map(product => 
                String(new Date(product.availableUntilDate).getFullYear()) // 연도를 문자열로 변환
            ))].sort((a, b) => b - a);

            // 데이터를 가져온 후 기본 연도 선택
            this.selectedYear = this.availableYears[0]; // 기본 연도 선택

            // 데이터 가공
            this.generateMonthlyData();
            this.generateProductGroupData();
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
      },
      generateMonthlyData() {
        const monthlyData = {};

        this.filteredProducts.forEach(product => {
          if (product.availableUntilDate) {
            const month = new Date(product.availableUntilDate)
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
      generateProductGroupData() {
        const productGroupMap = {};

        this.filteredProducts.forEach(product => {
          if (!productGroupMap[product.productName]) {
            productGroupMap[product.productName] = 1;
          } else {
            productGroupMap[product.productName] += 1;
          }
        });

        this.productGroupData = Object.keys(productGroupMap).map(productName => ({
          productName,
          value: productGroupMap[productName] // 물품 등장 횟수
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
      this.fetchProducts();
    }
  };
  </script>
  
<style scoped>
  .saleproduct-remain {
    padding: 20px;
  }
  
  .loading-message {
    font-size: 18px;
    color: #666;
  }

.charts-container {
  display: flex;
  justify-content: space-around; /* 차트를 양쪽에 배치 */
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
  
.product-table {
  width: 100%;
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
  