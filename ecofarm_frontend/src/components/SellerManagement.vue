<template>
  <div class="seller-management">
    <div v-if="errorMessage" class="error">
      <p>{{ errorMessage }}</p>
    </div>
    <h1>판매자 현황</h1>
    <div>
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
          <SellerBarChart v-if="monthlyData.length > 0" :monthlyData="monthlyData" :selectedYear="selectedYear" />
        </div>
        <div class="pie-chart-wrapper">
          <SellerPieChart v-if="apartGroupData.length > 0" :apartGroupData="apartGroupData" />
        </div>
      </div>
    </div>
    <!-- 승인된 판매자 섹션 -->
    <div v-if="approvedSellers.length > 0">
      <h3>승인된 판매자 : {{ approvedSellers.length }}명</h3>
      <table class="approved-seller-table">
        <thead>
          <tr>
            <th>등록번호</th>
            <th>이름</th>
            <th>이메일</th>
            <th>전화번호</th>
            <th>아파트 단지</th>
            <th>승인 날짜</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="seller in approvedSellers" :key="seller.sellerID">
            <td>판매자-{{ seller.sellerID }}</td>
            <td>{{ seller.sellerName }}</td>
            <td>{{ seller.email }}</td>
            <td>{{ seller.phoneNumber }}</td>
            <td>{{ seller.apartmentName }}</td>
            <td>{{ seller.approvedDate }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else>
      <p>승인된 판매자가 없습니다.</p>
    </div>
  </div>
</template>

<script>
import SellerBarChart from './SellerBarChart.vue'; // 경로 수정 필요
import SellerPieChart from './SellerPieChart.vue';

export default {
  components: {
    SellerBarChart,
    SellerPieChart
  },
  data() {
    return {
      pendingSellers: [],
      approvedSellers: [],
      errorMessage: '',
      monthlyData: [], // 월별 데이터 추가
      selectedYear: '2024', // 현재 연도로 초기화
      apartGroupData: [],
    };
  },
  methods: {
    async fetchApprovedSellers() {
      const token = sessionStorage.getItem('jwtToken');
      try {
        const response = await fetch('http://localhost:8080/api/sellers/approved', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        if (response.ok) {
          this.approvedSellers = await response.json();
          this.processApprovalData(); // 승인된 판매자 데이터 처리
          // 아파트별 판매자 수 데이터 계산
          const apartGroupData = this.getApartGroupData(this.approvedSellers);
          this.apartGroupData = apartGroupData;
        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '승인된 판매자 목록을 가져오는데 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to fetch approved sellers:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    processApprovalData() {
      const approvalCounts = {};

      this.approvedSellers.forEach(seller => {
        // approvedDate에서 연도와 월을 추출
        const date = new Date(seller.approvedDate);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0'); // 월을 숫자로 가져오고 두 자리 형식으로 변환
        const monthYear = `${year}-${month}`; // "연도-월" 형식으로 조합
        //this.selectedYear = year.toString();

        if (!approvalCounts[monthYear]) {
          approvalCounts[monthYear] = 0;
        }
        approvalCounts[monthYear]++;
      });

      // 데이터를 차트에 맞게 변환하고 월로 정렬
      this.monthlyData = Object.keys(approvalCounts)
        .map(monthYear => ({
          month: monthYear,
          count: approvalCounts[monthYear]
        }))
        .sort((a, b) => {
          const [yearA, monthA] = a.month.split('-').map(Number);
          const [yearB, monthB] = b.month.split('-').map(Number);
          return yearA === yearB ? monthA - monthB : yearA - yearB; // 연도와 월을 비교하여 정렬
        });
    },
    getApartGroupData(sellers) {
      const apartmentGroups = {};

      sellers.forEach(seller => {
        const apartmentName = seller.apartmentName;

        // 아파트 이름이 이미 객체에 존재하면 카운트를 증가시킴
        if (apartmentGroups[apartmentName]) {
          apartmentGroups[apartmentName]++;
        } else {
          // 새로운 아파트 이름이면 카운트를 1로 초기화
          apartmentGroups[apartmentName] = 1;
        }
      });

      // 객체를 배열로 변환하여 반환
      return Object.entries(apartmentGroups)
        .map(([apartmentName, count]) => ({ apartmentName, count }))
        .sort((a, b) => a.apartmentName.localeCompare(b.apartmentName));

    }
  },
  created() {
    this.fetchApprovedSellers();
  },
  computed: {
    availableYears() {
      // monthlyData에서 사용 가능한 연도를 추출하여 중복 제거 후 반환
      const years = this.monthlyData.map(data => data.month.split('-')[0]);
      return [...new Set(years)]; // 중복 제거
    }
  }
};
</script>

<style scoped>
.seller-management {
  padding: 20px;
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

.approved-seller-table {
  width: 100%;
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px; 
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.approved-seller-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.approved-seller-table th,
.approved-seller-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.approved-seller-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.approved-seller-table td:nth-child(6){
  text-align: right; /* 오른쪽 정렬 */
}

button {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #388e3c;
}

.error {
  color: #d32f2f;
  margin-bottom: 20px;
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
</style>
