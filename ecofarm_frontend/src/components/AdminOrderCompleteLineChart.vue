<template>
    <div>
      <h2>월별 판매량 추이</h2>
      <div class="line-chart-wrapper">
        <Line :data="lineChartData" :options="lineChartOptions" />
      </div>
    </div>
  </template>
  
  <script>
  import { Line } from 'vue-chartjs';
  import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, PointElement, LinearScale, CategoryScale } from 'chart.js';
  
  ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, LinearScale, CategoryScale);
  
  export default {
    components: {
      Line
    },
    props: {
      monthlyData: {
        type: Array,
        required: true
      }
    },
    data() {
      return {
        lineChartData: {
          labels: [...Array(12).keys()].map(i => `${i + 1}월`), // 월 레이블
          datasets: []
        },
        lineChartOptions: {
          responsive: true,
          plugins: {
            legend: {
              display: true,
              position: 'top'
            },
            title: {
              display: true,
              text: '월별 판매량'
            }
          },
          scales: {
            x: {
              title: {
                display: true,
                text: '월'
              }
            },
            y: {
              title: {
                display: true,
                text: '판매량'
              },
              beginAtZero: true
            }
          }
        }
      };
    },
    watch: {
      monthlyData: {
        immediate: true,
        handler() {
          this.updateLineChartData();
        }
      }
    },
    methods: {
        updateLineChartData() {
            const currentYear = new Date().getFullYear(); // 현재 연도
            const maxMonths = 12; // 최대 12개월

            // 월별 데이터와 레이블 필터링
            this.lineChartData.labels = [...Array(maxMonths).keys()]
            .map(i => `${i + 1}월`); // 월 레이블 생성

            // 각 연도에 대해 데이터가 있는 월만 필터링
            this.lineChartData.datasets = this.monthlyData.map(yearData => {
            // 데이터가 있는 월만 포함
            const filteredData = yearData.data.slice(0, 12).map((value, index) => {
                // 0이 아닌 값만 포함하여 그에 맞는 레이블과 데이터만 반환
                return value > 0 ? value : null; // 0이면 null로 설정
            });

            return {
                label: `${yearData.year}년`,
                data: filteredData,
                borderColor: yearData.year === currentYear ? '#4caf50' : '#add8e6', // 색상 설정
                fill: false // 영역 채우기
            };
            });

            // 레이블과 데이터가 null이 아닌 것만 필터링하여 실제로 보여줄 값 결정
            this.lineChartData.labels = this.lineChartData.labels.filter((_, index) => {
            return this.lineChartData.datasets.some(dataset => dataset.data[index] !== null);
            });

            // 데이터가 null인 월의 데이터도 제거
            this.lineChartData.datasets.forEach(dataset => {
            dataset.data = dataset.data.filter(value => value !== null);
            });
        }
    }
  };
  </script>
  
  <style scoped>
  .line-chart-wrapper {
    width: 100%; /* 부모 요소의 너비를 100%로 설정 */
    height: 400px; /* 원하는 높이를 설정 (예: 400px) */
  }
  </style>
  