<template>
  <div class="consumer-page">
    <TitleBar />
    <div class="consumer-content">
      <!-- Sidebar Navigation -->
      <div class="sidebar">
        <div class="sidebar-title">소비자 페이지</div>
        <ul>
          <li @click="showSection('saleproduct')" :class="{ active: currentSection === 'saleproduct' }">판매 물품 보기</li>
          <li @click="showSection('order')" :class="{ active: currentSection === 'order' }">주문 관리</li>
          <li @click="showSection('review')" :class="{ active: currentSection === 'review' }">리뷰 관리</li>
          <li @click="showSection('profile')" :class="{ active: currentSection === 'profile' }">정보 수정</li>
        </ul>
      </div>
      <!-- Content Area -->
      <div class="content">
        <component 
          :is="currentSectionComponent" 
          :saleProduct="selectedSaleProduct" 
          :order="currentSection === 'reviewWrite' ? selectedOrder : null" 
          :changeSection="showSection" 
        />
      </div>
    </div>
  </div>
</template>

<script>
import TitleBar from './TitleBar.vue';
import consumerProfile from './ConsumerProfilePage.vue';
import SaleProductList from './SaleProductList.vue';
import SaleProductDetail from './SaleProductDetailPage.vue';
import OrderList from './ConsumerOrderList.vue';
import ReviewWrite from './ReviewWritePage.vue';
import ReviewList from './ConsumerReviewList.vue';

export default {
  components: {
    TitleBar,
    consumerProfile,
    SaleProductList,
    SaleProductDetail,
    OrderList,
    ReviewWrite,
    ReviewList,
  },
  data() {
    return {
      currentSection: 'saleproduct',
      selectedSaleProduct: null,
      selectedOrder: null
    };
  },
  computed: {
    currentSectionComponent() {
      return {
        'profile': 'consumerProfile',
        'saleproduct': 'SaleProductList',
        'saleproductdetail': 'SaleProductDetail',
        'order': 'OrderList',
        'reviewWrite': 'ReviewWrite',
        'review': 'ReviewList',
      }[this.currentSection];
    }
  },
  methods: {
    showSection(section, SaleProduct = null, Order = null) {
      this.currentSection = section;
      this.selectedSaleProduct = SaleProduct;
      this.selectedOrder = Order;
    }
  }
};
</script>

<style scoped>
.consumer-page {
  display: flex;
  flex-direction: column;
  height: 98.5vh;
}

.consumer-content {
  display: flex;
  flex: 1;
}

.sidebar {
  width: 200px;
  background: linear-gradient(to bottom, rgb(76, 175, 80), rgb(127, 223, 130)); /* 그라데이션 */
  padding: 20px;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: white; /* 하얀색 글씨 */
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
  color: white; /* 하얀색 글씨 */
}

.sidebar li:hover,
.sidebar li.active {
  background-color: rgb(56, 133, 58); /* 밝은 초록색 */
}

.content {
  flex: 1;
  padding: 20px;
  background-color: rgb(235, 241, 235); /* 콘텐츠 영역 배경색 */
}
</style>
