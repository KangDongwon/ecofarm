<template>
    <div class="admin-page">
      <TitleBar />
      <div class="admin-content">
        <!-- 사이드바 네비게이션 -->
        <div class="sidebar">
          <div class="sidebar-title">관리자 대시보드</div>
          <ul>
            <li class ="main-menu">사업 관리</li>
            <ul class="sub-menu">
              <li @click="showSection('apartment')" :class="{ active: currentSection === 'apartment' }">아파트 계약</li>
              <li @click="showSection('product')" :class="{ active: currentSection === 'product' }">재배 물품</li>
            </ul>
            <li class ="main-menu">회원 관리</li>
            <ul class="sub-menu">
              <li @click="showSection('consumer')" :class="{ active: currentSection === 'consumer' }">소비자 현황</li>
              <li @click="showSection('seller')" :class="{ active: currentSection === 'seller' }">판매자 현황</li>
              <li @click="showSection('sellerapprove')" :class="{ active: currentSection === 'sellerapprove' }">판매자 승인</li>
            </ul>
            <li class ="main-menu">판매 물품 관리</li>
            <ul class="sub-menu">
              <li @click="showSection('saleproduct')" :class="{ active: currentSection === 'saleproduct' }">판매 물품 현황</li>
              <li @click="showSection('saleproductapprove')" :class="{ active: currentSection === 'saleproductapprove' }">판매 물품 승인</li>
              <li @click="showSection('saleproductremain')" :class="{ active: currentSection === 'saleproductremain' }">재고 물품 현황</li>
            </ul>
            <li class ="main-menu">주문 관리</li>
            <ul class="sub-menu">
              <li @click="showSection('orderapprove')" :class="{ active: currentSection === 'orderapprove' }">물품 전달 확인</li>
              <li @click="showSection('ordercomplete')" :class="{ active: currentSection === 'ordercomplete' }">구매 현황</li>
              <li @click="showSection('order')" :class="{ active: currentSection === 'order' }">진행 중인 주문</li>
            </ul>
            <li class ="main-menu">데이터 분석</li>
            <ul class="sub-menu">
              <li @click="showSection('orderanalysis')" :class="{ active: currentSection === 'orderanalysis' }">수요 분석</li>
              <li @click="showSection('customerSatisfaction')" :class="{ active: currentSection === 'customerSatisfaction' }">고객만족도 분석</li>
            </ul>
          </ul>
        </div>
        <!-- 컨텐츠 영역 -->
        <div class="content">
          <component :is="currentSectionComponent" />
        </div>
      </div>
    </div>
</template>
  
<script>
  import TitleBar from './TitleBar.vue';
  import ApartmentManagement from './ApartmentManagement.vue';
  import SellerManagement from './SellerManagement.vue';
  import ProductManagement from './ProductManagement.vue';
  import SaleProductManagementAdmin from './SaleProductManagementAdmin.vue';
  import OrderList from './AdminOrderList.vue';
  import ConsumerManagement from './ConsumerManagement.vue';
  import SellerApproveList from './SellerApproveList.vue';
  import SaleProductApproveList from './SaleProductApproveList.vue';
  import SaleProductRemain from './SaleProductRemain.vue';
  import AdminOrderApprove from './AdminOrderApprove.vue';
  import AdminOrderComplete from './AdminOrderComplete.vue';
  import AdminOrderAnalysis from './AdminOrderAnalysis.vue';
  import CustomerSatisfaction from './CustomerSatisfaction.vue';
  
  export default {
    components: {
      TitleBar,
      ApartmentManagement,
      ConsumerManagement,
      SellerManagement,
      ProductManagement,
      SaleProductManagementAdmin,
      OrderList,
      SellerApproveList,
      SaleProductApproveList,
      SaleProductRemain,
      AdminOrderApprove,
      AdminOrderComplete,
      AdminOrderAnalysis,
      CustomerSatisfaction,
    },
    data() {
      return {
        currentSection: 'apartment'
      };
    },
    computed: {
      currentSectionComponent() {
        return {
          'apartment': 'ApartmentManagement',
          'product': 'ProductManagement',
          'consumer': 'ConsumerManagement',
          'seller': 'SellerManagement',
          'saleproduct': 'SaleProductManagementAdmin',
          'order': 'OrderList',
          'sellerapprove': 'SellerApproveList',
          'saleproductapprove': 'SaleProductApproveList',
          'saleproductremain': 'SaleProductRemain',
          'orderapprove': 'AdminOrderApprove',
          'ordercomplete': 'AdminOrderComplete',
          'orderanalysis': 'AdminOrderAnalysis',
          'customerSatisfaction': 'CustomerSatisfaction',
        }[this.currentSection];
      }
    },
    methods: {
      showSection(section) {
        this.currentSection = section;
      }
    }
  };
</script>
  
<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.admin-content {
  display: flex;
  flex: 1;
}

.sidebar {
  width: 200px;
  /* background: linear-gradient(to bottom, rgb(76, 175, 80), rgb(127, 223, 130)); */ /* 그라데이션 */
  background: rgb(76, 175, 80);
  padding: 20px;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: black; /* 하얀색 글씨 */
}

.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar li {
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

/* sub-menu에 대해서만 hover와 active 상태에 스타일을 적용 */
.sub-menu li:hover,
.sub-menu li.active {
  background-color: rgb(56, 133, 58); /* 밝은 초록색 */
}

.sub-menu li {
  color: white; /* 하얀색 글씨 */
  padding-left: 35px; /* 하위 메뉴 들여쓰기 */
  padding-top: 6px;
  padding-bottom: 6px;
}

.main-menu {
  color: black;
  font-weight: bold; /* 텍스트를 볼드 처리 */
  letter-spacing: 1.5px; /* 글자 간격 */
}

.content {
  flex: 1;
  padding: 20px;
  /*background-color: rgb(235, 241, 235);*/ /* 콘텐츠 영역 배경색 */
  background-color: rgb(245, 245, 217);
}
</style>

  