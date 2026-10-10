<template>
  <div class="seller-page">
    <TitleBar />
    <div class="seller-content">
      <!-- Sidebar Navigation -->
      <div class="sidebar">
        <div class="sidebar-title">판매자 페이지</div>
        <ul>
          <li @click="showSection('saleproduct')" :class="{ active: currentSection === 'saleproduct' }">판매 물품 관리</li>
          <li @click="showSection('order')" :class="{ active: currentSection === 'order' }">주문 관리</li>
          <li @click="showSection('profile')" :class="{ active: currentSection === 'profile' }">판매자 정보 관리</li>
        </ul>
      </div>
      <!-- Content Area -->
      <div class="content">
        <component :is="currentSectionComponent" />
      </div>
    </div>
  </div>
</template>

<script>
import TitleBar from './TitleBar.vue';
import SellerProfile from './SellerProfilePage.vue';
import SaleProductManagement from './SaleProductManagement.vue';
import OrderList from './SellerOrderList.vue';

export default {
  components: {
    TitleBar,
    SellerProfile,
    SaleProductManagement,
    OrderList
  },
  data() {
    return {
      currentSection: 'saleproduct'
    };
  },
  computed: {
    currentSectionComponent() {
      return {
        'profile': 'SellerProfile',
        'saleproduct': 'SaleProductManagement', 
        'order': 'OrderList'
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
.seller-page {
  display: flex;
  flex-direction: column;
  height: 98.5vh; /* 변경된 높이 */
}

.seller-content {
  display: flex;
  flex: 1;
}

.sidebar {
  width: 200px;
  background: linear-gradient(to bottom, rgb(76, 175, 80), rgb(127, 223, 130)); /* 그라데이션 효과 */
  padding: 20px;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: white; /* 흰색 텍스트 */
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
  color: white; /* 흰색 텍스트 */
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
