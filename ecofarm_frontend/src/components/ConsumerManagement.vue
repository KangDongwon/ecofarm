<template>
  <div class="consumer-management">
    <div v-if="errorMessage" class="error">
      <p>{{ errorMessage }}</p>
    </div>
    <h1>소비자 현황</h1>
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
          <BarChart v-if="monthlyData.length > 0" :monthlyData="monthlyData" :selectedYear="selectedYear" />
        </div>
        <div class="pie-chart-wrapper">
          <PieChart v-if="ageGroupData.length > 0" :ageGroupData="ageGroupData" />
        </div>
      </div>
    </div>
    <h3>총 회원 수: {{ consumers.length }}명</h3>
    <table class="consumer-table">
      <thead>
        <tr>
          <th>이름</th>
          <th>이메일</th>
          <th>전화번호</th>
          <th>주소</th>
          <th>나이</th>
          <th>관심질환</th>
          <th>가입일자</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="consumer in consumers" :key="consumer.consumerID">
          <td>{{ consumer.consumerName }}</td>
          <td>{{ consumer.email }}</td>
          <td>{{ consumer.phoneNumber }}</td>
          <td>{{ consumer.address }}</td>
          <td>{{ consumer.age }}</td>
          <td>{{ consumer.diseases }}</td>
          <td>{{ consumer.signupDate }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import BarChart from './ConsumerBarChart.vue'; // BarChart 컴포넌트 가져오기
import PieChart from './ConsumerPieChart.vue'; // PieChart 컴포넌트 가져오기

export default {
  components: {
    BarChart,
    PieChart
  },
  data() {
    return {
      consumers: [],
      ageGroupData: [],
      monthlyData: [],
      selectedYear: '', // 선택된 연도
      errorMessage: ''
    };
  },
  methods: {
    async fetchConsumers() {
      const token = sessionStorage.getItem('jwtToken');
      try {
        const response = await fetch('http://localhost:8080/api/consumers', {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        if (response.ok) {
          const js = await response.json();

          // 이메일이 있는 소비자만 필터링 및 가입일자 형식 변환
          this.consumers = js
            .filter(consumer => consumer.email)
            .map(consumer => ({
              ...consumer,
              signupDate: new Date(consumer.signupDate)
                .toISOString()
                .split('T')[0] // 'yyyy-MM-dd' 형식으로 변환
            }));

          // 나이대별 소비자 수 데이터 계산
          const ageGroupData = this.getAgeGroupData(this.consumers);
          this.ageGroupData = ageGroupData; // ageGroupData 설정
          
          // 월별 가입자 수 데이터 가져오기
          this.monthlyData = this.getMonthlyData(js);

          // 데이터를 가져온 후 기본 연도 선택
          this.selectedYear = this.availableYears[1]; // 기본 연도 선택
        } else {
          // 에러 처리
          const errorData = await response.json();
          this.errorMessage =
            errorData.message || '소비자 목록을 가져오는데 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to fetch consumers:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    getMonthlyData(consumers) {
      const monthlyData = {};
      consumers.forEach(consumer => {
        if (consumer.signupDate) {
          const month = new Date(consumer.signupDate)
            .toISOString()
            .split('T')[0]
            .slice(0, 7); // 'yyyy-MM' 형식으로 변환
          if (!monthlyData[month]) {
            monthlyData[month] = 0;
          }
          monthlyData[month]++;
        }
      });

      // Object.entries로 데이터를 배열로 변환한 후 month를 기준으로 정렬
      return Object.entries(monthlyData)
        .sort(
          ([monthA], [monthB]) =>
            new Date(`${monthA}-01`) - new Date(`${monthB}-01`)
        ) // 월별 정렬
        .map(([month, count]) => ({
          month,
          count
        }));
    },
    getAgeGroupData(consumers) {
      const ageGroups = {
        '30대': 0,
        '40대': 0,
        '50대': 0,
        '60대': 0,
        '70대': 0,
        '80대 이상': 0
      };

      consumers.forEach(consumer => {
        const age = consumer.age;
        if (age >= 30 && age < 40) {
          ageGroups['30대']++;
        } else if (age >= 40 && age < 50) {
          ageGroups['40대']++;
        } else if (age >= 50 && age < 60) {
          ageGroups['50대']++;
        } else if (age >= 60 && age < 70) {
          ageGroups['60대']++;
        } else if (age >= 70 && age < 80) {
          ageGroups['70대']++;
        } else if (age >= 80 && age < 90) {
          ageGroups['80대 이상']++;
        }
      });

      return Object.entries(ageGroups).map(([ageGroup, count]) => ({ ageGroup, count }));
    }
  },
  computed: {
    availableYears() {
      // monthlyData에서 사용 가능한 연도를 추출하여 중복 제거 후 반환
      const years = this.monthlyData.map(data => data.month.split('-')[0]);
      return [...new Set(years)]; // 중복 제거
    }
  },
  mounted() {
    this.fetchConsumers(); // 소비자 목록을 가져옴
  }
};
</script>

<style scoped>
.consumer-management {
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

.consumer-table {
  width: 100%;
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px; 
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.consumer-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.consumer-table th,
.consumer-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.consumer-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.consumer-table td:nth-child(5), 
.consumer-table td:nth-child(7) {
  text-align: right; /* 오른쪽 정렬 */
}


.error {
  color: #d32f2f; /* 붉은색으로 에러 메시지 강조 */
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
