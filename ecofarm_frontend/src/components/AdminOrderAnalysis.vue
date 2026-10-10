<template>
  <div class="order-analysis">
    <h1>수요 분석</h1>

    <!-- 차트들을 나란히 표시하기 위한 div -->
    <div class="charts-container">
      <div class="line-chart-wrapper">
        <AdminOrderCompleteLineChart v-if="monthlyData.length > 0" :monthlyData="monthlyData" />
      </div>
      <div class="pie-chart-wrapper">
        <AdminOrderCompletePieChartApart v-if="apartGroupData.length > 0" :apartGroupData="apartGroupData" />
      </div>
      <div class="pie-chart-wrapper">
        <AdminOrderCompletePieChartProduct v-if="productGroupData.length > 0" :productGroupData="productGroupData" />
      </div>
    </div>

    <div class="aitems">
      <div class="title-container">
        <h2>AITEMS 추천 품목</h2> <!-- 제목은 한 줄에 따로 배치 -->
        <button class="submit-button" @click="submitSelection">AITEMS 추천 품목 생성</button>
      </div>
      <div class="form-row">
        <div class="form-group">
          <select v-model="selectedProductGroup" id="productGroup">
            <option value="" disabled selected>품목 선택</option> <!-- 기본 라벨 옵션 -->
            <option value="1">브로콜리</option>
            <option value="2">팥순</option>
            <option value="3">케일</option>
            <option value="4">적양배추</option>
            <option value="5">무순</option>
            <option value="6">해바라기</option>
            <option value="7">완두콩</option>
            <option value="8">시금치</option>
            <option value="9">와사비</option>
            <option value="10">루꼴라</option>
            <option value="11">비트</option>
            <option value="12">아루굴라</option>
            <option value="13">유채</option>
          </select>
        </div>
        <div class="form-group">
          <select v-model="selectedAgeGroup" id="ageGroup">
            <option value="" disabled selected>연령 선택</option> <!-- 기본 라벨 옵션 -->
            <option value="30">30대</option>
            <option value="40">40대</option>
            <option value="50">50대</option>
            <option value="60">60대</option>
            <option value="70">70대</option>
            <option value="80">80대 이상</option>
          </select>
        </div>
        <div class="form-group">
          <label for="diseases" class="dis_title">관심 질환 선택</label>
          <div class="checkbox-group">
            <div v-for="disease in diseaseOptions" :key="disease" class="checkbox-item">
              <input type="checkbox" 
                    :value="disease" 
                    v-model="selectedDiseases" />
              <span class="checkbox-label">{{ disease }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="aitems-list">
      <h3>추천 품목 리스트</h3>
      <table v-if="recommendedItems.length > 0" class="recommended-table">
        <thead>
          <tr>
            <th>물품번호</th>
            <th>이름</th>
            <th>효능</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in recommendedItems" :key="item.id">
            <td>물품번호-{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.benefit }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 로딩 중 메시지 표시 -->
    <div v-if="loading" class="loading-spinner-container">
      <div class="loading-spinner"></div> <!-- 로딩 스피너 -->
    </div>

    <!-- 에러 메시지 표시 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script>
import AdminOrderCompleteLineChart from './AdminOrderCompleteLineChart.vue';
import AdminOrderCompletePieChartApart from './AdminOrderCompletePieChartApart.vue';
import AdminOrderCompletePieChartProduct from './AdminOrderCompletePieChartProduct.vue';

export default {
  props: ['changeSection'],
  components: {
    AdminOrderCompletePieChartApart,
    AdminOrderCompletePieChartProduct,
    AdminOrderCompleteLineChart,
  },
  data() {
    return {
      orders: [], // 주문 목록
      errorMessage: null,
      selectedYear: '',
      apartGroupData: [],
      productGroupData: [],
      monthlyData: [],
      loading: true,
      selectedProductGroup: '', // 품목 선택
      selectedAgeGroup: '', // 연령 선택
      diseaseOptions: ['비만', '당뇨', '고혈압', '고지혈증', '심혈관질환', '암', '치매', '염증', '간질환'], // 질환 옵션
      selectedDiseases: [],
      recommendedItems : [],
      productInfo: [
        { id: "1", name: "브로콜리", benefit: "굴루코라파닌 성분, 항암 효과" },
        { id: "2", name: "팥순", benefit: "아주카사포닌 성분, 비만 예방" },
        { id: "3", name: "케일", benefit: "루테인-지아잔틴 성분, 항산화-항염증 효과" },
        { id: "4", name: "적양배추", benefit: "글루코시놀레이트 성분, 심혈관질환(LDL) 예방" },
        { id: "5", name: "무순", benefit: "설포라판 성분, 전립선암-유방암 예방" },
        { id: "6", name: "해바라기", benefit: "비타민E, 셀레늄 성분, 심혈관 건강 증진" },
        { id: "7", name: "완두콩", benefit: "단백질 성분, 체중-혈당 관리" },
        { id: "8", name: "시금치", benefit: "철분-엽산 성분, 고혈압 예방" },
        { id: "9", name: "와사비", benefit: "글루코시놀레이트 성분, 심혈관질환 건강 개선" },
        { id: "10", name: "루꼴라", benefit: "루테인-베타카로틴 성분, 항암 효과" },
        { id: "11", name: "비트", benefit: "질산염 성분, 심혈관 건강 개선" },
        { id: "12", name: "아루굴라", benefit: "글루코시놀레이트 성분, 항암 효과, 심혈관 건강 개선" },
        { id: "13", name: "유채", benefit: "비타민 C,K 성분, 심장병 예방, 항염 효과" }
      ] // 선택된 질환
    };
  },
  computed: {
    filteredOrders() {
      return this.orders
          .filter(order => order.status === '물품 전달 완료')
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

          // 데이터 가공
          this.groupByApartmentName();
          this.groupByProdcut();
          this.groupByMonth();
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
    groupByMonth() {
      const monthlyData = {
        2023: Array(12).fill(0), // 2023년 월별 초기화
        2024: Array(12).fill(0)  // 2024년 월별 초기화
      };

      this.filteredOrders.forEach((order) => {
        const year = new Date(order.orderDate).getFullYear(); // 주문 연도
        const month = new Date(order.orderDate).getMonth(); // 주문 월 (0-11)
        
        if (year === 2023 || year === 2024) {
          monthlyData[year][month] += 1; // 해당 월에 1 추가
        }
      });

      // monthlyData를 차트 데이터로 변환
      this.monthlyData = [
        { year: 2023, data: monthlyData[2023] },
        { year: 2024, data: monthlyData[2024] }
      ];
    },
    async submitSelection() {
       // 입력값 체크
      if (!this.selectedProductGroup && !this.selectedAgeGroup && this.selectedDiseases.length === 0) {
        alert('하나 이상의 옵션을 선택해 주세요.'); // 알림 팝업
        return; // 함수 종료
      }

      const requestData = {
        productGroup: this.selectedProductGroup,
        ageGroup: this.selectedAgeGroup,
        diseases: this.selectedDiseases,
      };

      try {
        this.loading = true; // 로딩 시작
        // API 호출하여 선택한 데이터 전송
        const response = await fetch(`http://localhost:8080/api/aitems`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}`
          },
          body: JSON.stringify(requestData),
        });

        if (response.ok) {
          const responseData = await response.json();
          console.log(responseData);
          this.recommendedItems = this.mapIdsToProducts(responseData.values); // 응답 데이터를 저장
          
          // 응답 번호를 기반으로 품목 정보 가져오기
          this.selectedProducts = this.recommendedItems.map(id => {
            const product = this.productInfo.find(item => item.id === id);
            return product ? `${product.name} (${product.benefit})` : null;
          }).filter(item => item !== null);

        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        }else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '추천 품목 요청에 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to submit selection:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      } finally {
        this.loading = false;
      }
    },
    mapIdsToProducts(ids) {
      return ids.map(id => {
        return this.productInfo.find(product => product.id === id) || null;
      }).filter(item => item !== null); // null 제거
    },
  },
  created() {
    this.fetchOrders();
  }
};
</script>

<style scoped>
.order-analysis {
padding: 20px;
}

.charts-container {
display: flex;
justify-content: center; 
flex-wrap: wrap; /* 화면이 작아질 경우 차트를 줄 바꿈 */
margin-bottom: 20px;
}

.line-chart-wrapper {
  flex: 1;
  min-width: 200px;
  max-width: 800px;
  margin-right: 40px; /* 라인 차트와 파이 차트 사이에 여백 추가 */
}

.pie-chart-wrapper {
  flex: 1;
  min-width: 200px;
  max-width: 400px;
  margin: 0; /* 파이 차트끼리의 간격을 줄임 */
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

.error-message {
  color: red;
  margin-top: 10px;
}

@keyframes spin {
0% { transform: rotate(0deg); }
100% { transform: rotate(360deg); }
}

.aitems {
  margin-bottom: 20px;
  text-align: left;
}

.form-row {
  display: flex; /* 가로로 정렬 */
  justify-content:flex-start; /* 중앙 정렬 */
  align-items: center;
  gap: 20px; /* 항목 간격 */
  background-color: #66bb6a;
  padding: 10px;
  border-radius: 10px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

select {
  width: 200px; /* 각 입력 요소의 너비 */
  padding: 10px;
  font-size: 16px;
  border: 1px solid #145e17;
  border-radius: 5px;
  background-color: #ffffff;
  margin-right: 30px;
}

.dis_title {
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 10px;
  color: white;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
}

.checkbox-item {
  display: flex;
  align-items: center; /* 체크박스와 라벨을 수직으로 정렬 */
  margin-right: 20px;
}

.checkbox-label {
  margin-left: 5px; /* 체크박스와 라벨 간격 조정 */
  display: inline; /* 라벨이 줄 바꿈되지 않도록 설정 */
  white-space: nowrap; /* 라벨 텍스트가 줄 바꿈되지 않도록 설정 */
  font-weight: bold;
  color: #f3f3f3;
}

.submit-button {
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

.submit-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

.title-container {
  display: flex; /* 가로로 정렬 */
  align-items: center; /* 세로로 가운데 정렬 */
  gap: 10px; /* 제목과 버튼 간의 간격 */
  margin-bottom: 20px; /* 제목과 다음 요소 간의 간격 */
}

.recommended-table {
  width: 100%;
  table-layout: fixed; /* 고정 레이아웃 */
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px;
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.recommended-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.recommended-table th,
.recommended-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.recommended-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

</style>

