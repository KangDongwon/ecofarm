<template>
    <h2>월별 판매자 승인 현황</h2>
    <div class="bar-chart-wrapper"> <!-- 바 차트를 감싸는 wrapper 추가 -->
      <Bar :data="chartData" :options="chartOptions" />
    </div>
  </template>
  
  <script>
  import { Bar } from 'vue-chartjs';
  import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js';
  
  // Chart.js 구성 요소 등록
  ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);
  
  export default {
    components: {
      Bar
    },
    props: {
      monthlyData: {
        type: Array,
        required: true
      },
      selectedYear: {
        type: String,
        required: true
      }
    },
    data() {
      return {
        chartData: {
          labels: [], // 차트의 라벨
          datasets: [
            {
              label: '승인 수', // 데이터셋 라벨
              backgroundColor: '#4caf50', // 데이터셋 색상
              data: [] // 데이터 배열
            }
          ]
        },
        chartOptions: {
          responsive: true, // 반응형 차트
          plugins: {
            legend: {
              display: true, // 범례 표시 여부
              position: 'top' // 범례 위치
            },
            title: {
              display: true, // 제목 표시 여부
              text: '월별 승인된 판매자 수' // 차트 제목
            }
          },
          scales: {
            x: {
              title: {
                display: true,
                text: '' // X축 제목
              }
            },
            y: {
              title: {
                display: true,
                text: '' // Y축 제목
              },
              beginAtZero: true // Y축 0부터 시작
            }
          }
        }
      };
    },
    watch: {
      monthlyData: {
        immediate: true, // 초기 데이터 변경 시 즉시 호출
        handler() {
          this.updateChartData(); // 데이터 변경 시 차트 업데이트
        }
      },
      selectedYear(newYear) {
        this.updateChartData(); // selectedYear가 바뀔 때마다 차트 업데이트
      }
    },
    methods: {
      updateChartData() {
        // selectedYear에 따라 monthlyData 필터링
        const filteredData = this.monthlyData.filter(data => data.month.startsWith(this.selectedYear));
        console.table(this.monthlyData);
        //console.log('here ' + this.selectedYear);
        // 필터링된 데이터가 없으면 차트 데이터를 빈 값으로 설정
        if (filteredData.length === 0) {
          this.chartData.labels = ['데이터 없음']; // 빈 라벨 설정
          this.chartData.datasets[0].data = [0]; // 빈 데이터 설정
          return; // 더 이상 진행하지 않음
        }
  
        const newLabels = filteredData.map(data => data.month); // 필터링된 데이터에서 월 정보 추출
        const newData = filteredData.map(data => data.count); // 필터링된 데이터에서 수치 정보 추출
  
        // 데이터가 변경된 경우에만 업데이트
        if (
          JSON.stringify(newLabels) !== JSON.stringify(this.chartData.labels) ||
          JSON.stringify(newData) !== JSON.stringify(this.chartData.datasets[0].data)
        ) {
          // 새로운 객체로 대체하여 Vue의 반응성을 유지
          this.chartData = {
            labels: [...newLabels], // 새로운 배열로 대체
            datasets: [
              {
                label: '승인된 판매자 수',
                backgroundColor: '#4caf50',
                data: [...newData] // 새로운 배열로 대체
              }
            ]
          };
        }
      }
    },
    mounted() {
      this.updateChartData(); // 컴포넌트가 마운트되면 초기 차트 데이터 설정
    }
  };
  </script>
  
  <style scoped>
  .bar-chart-wrapper {
    width: 100%; /* 부모 요소의 너비를 100%로 설정 */
    height: 400px; /* 원하는 높이를 설정 (예: 400px) */
  }
  </style>
  