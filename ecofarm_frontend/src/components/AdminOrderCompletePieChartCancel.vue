<template>
    <h2>구매 완료/취소 현황</h2>
    <div class="pie-chart-wrapper">
      <Pie :data="chartData" :options="chartOptions" />
    </div>
</template>
  
<script>
  import { Pie } from 'vue-chartjs';
  import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement } from 'chart.js';
  import ChartDataLabels from 'chartjs-plugin-datalabels'; // 데이터 레이블 플러그인 임포트
  
  ChartJS.register(Title, Tooltip, Legend, ArcElement, ChartDataLabels); // 플러그인 등록
  
  export default {
    components: {
      Pie
    },
    props: {
      cancelGroupData: {
        type: Array,
        required: true
      }
    },
    data() {
      return {
        chartData: {
          labels: [],
          datasets: [
            {
              label: '아파트별 물품 수',
              //backgroundColor: ['#ff6384', '#36a2eb', '#ffce56', '#4caf50', '#ffa500'],
              data: []
            }
          ]
        },
        chartOptions: {
          responsive: true,
          plugins: {
            legend: {
              display: false,
              position: 'top'
            },
            title: {
              display: true,
              text: '구매 완료/취소'
            },
            // 데이터 레이블 설정
            datalabels: {
              formatter: (value, context) => {
                const total = context.chart.data.datasets[0].data.reduce((acc, val) => acc + val, 0);
                const percentage = ((value / total) * 100).toFixed(2) + '%';
                const status = context.chart.data.labels[context.dataIndex]; // 아파트명 가져오기
              
                return `${status}\n${percentage}`;
              },
              color: '#000', // 텍스트 색상
              anchor: 'end', // 텍스트 위치 조정
              align: 'end',  // 텍스트 정렬 조정
              offset: -120, // 오프셋 조정
              font: {
                size: 15,
                weight: 'bold', // 글자 두께 조정
              },
              padding: 10 // 텍스트와 조각 사이의 패딩
            }
          }
        }
      };
    },
    watch: {
      apartGroupData: {
        immediate: true,
        handler() {
          this.updateChartData();
        }
      }
    },
    methods: {
      updateChartData() {
        //console.log(this.cancelGroupData);
        const labels = this.cancelGroupData.map(data => data.name); // 아파트명
        const data = this.cancelGroupData.map(data => data.value); // 아파트별 물품 수
        
        // 색상을 동적으로 생성하거나 정적으로 설정할 수 있습니다.
        const backgroundColors = labels.map((_, index) => {
            const colors = [
                '#90EE90', 
                '#D3D3D3', 
            ]; // 구별되는 색상 목록
            return colors[index % colors.length]; // 색상 목록에서 색상 선택
        });

        this.chartData.labels = labels;
        this.chartData.datasets[0].data = data;
        this.chartData.datasets[0].backgroundColor = backgroundColors; // 동적으로 색상 설정
      }
    }
  };
  </script>
  
  <style scoped>
  .pie-chart-wrapper {
    width: 100%; /* 부모 요소의 너비를 100%로 설정 */
    height: 400px; /* 원하는 높이를 설정 (예: 400px) */
  }
</style>
  