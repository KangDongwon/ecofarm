<template>
  <div class="saleproduct-list">
    <h2>판매 물품 리스트</h2>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <div v-if="products.length === 0" class="no-products-message">
      판매 중인 물품이 없습니다.
    </div>

    <div v-else class="product-grid">
      <div class="product-card" v-for="(product, index) in products" :key="product.saleProductID">
        <div class="card-content">
          <img class="product-image" :src="getImageUrl(product.productName)" alt="상품 이미지" />
          <div class="product-info">
            <h3>{{ product.productName }}</h3>
            <p><strong>아파트명:</strong> {{ product.apartmentName }}</p>
            <p><strong>판매자명:</strong> {{ product.sellerName }}</p>
            <p><strong>유효기간:</strong> {{ product.availableUntilDate }}</p>
            <p><strong>가격:</strong> {{ formatPrice(product.price) }}</p>
            <button @click="viewProductDetails(product)" class="toggle-button">상세보기</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

  
<script>
  export default {
    props: ['changeSection'],
    data() {
      return {
        filename: 'bro',
        products: [], // 제품 목록
        errorMessage: null
      };
    },
    methods: {
      async fetchProducts() {
        try {
            //console.log(sessionStorage.getItem('jwtToken'));
            // API 호출하여 제품 목록을 가져옵니다
            const response = await fetch('http://localhost:8080/api/saleproducts/approved', {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}` }
            });
            if (response.ok) {
              const allProducts = await response.json();
              // 현재 날짜 가져오기
              const currentDate = new Date();
              // availableUntilDate가 현재 날짜 이후인 제품만 필터링
              this.products = allProducts.filter(product => new Date(product.availableUntilDate) >= currentDate);
            } else if (response.status === 401) {
                this.$store.dispatch('logout');
                alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
                this.$router.push('/login');
            } else {
                const errorData = await response.json();
                this.errorMessage = errorData.message || '판매 물품 리스트를 가져오는데 실패했습니다.';
            }
        } catch (error) {
            console.error('Failed to fetch saleproducts:', error);
            this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
            this.products = [];
        }
      },
      getImageUrl(productName) {
        return new URL(`../assets/${productName}.jpg`, import.meta.url).href;
      },
      formatPrice(value) {
        return new Intl.NumberFormat().format(value);
      },
      viewProductDetails(saleproduct) {
        this.changeSection('saleproductdetail', saleproduct, null);
      }
    },
    created() {
      this.fetchProducts();
    }
  };
</script>
  
<style scoped>
.saleproduct-list {
  padding: 20px;
  background-color: #f9f9f9; /* 연한 배경색 */
}

.product-grid {
  display: flex;
  flex-wrap: wrap; /* 여러 줄로 나열 */
  gap: 20px; /* 카드 간의 간격 */
  justify-content: flex-start; /* 좌측 정렬 */
}

.product-card {
  flex: 1 1 1 1; /* 기본적으로 4개씩 표시 */
  max-width: 300px; /* 최대 너비 설정 */
  border: 1px solid #e0e0e0; /* 테두리 */
  border-radius: 10px; /* 모서리 둥글게 */
  padding: 16px; /* 패딩 */
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); /* 그림자 효과 */
  background-color: #ffffff; /* 흰색 배경 */
  transition: transform 0.2s; /* 마우스 호버 시 애니메이션 */
}

.product-card:hover {
  transform: scale(1.05); /* 카드 확대 효과 */
}

.product-card h3 {
  font-size: 1.5rem; /* 제목 글자 크기 */
  color: #4CAF50; /* 채소 느낌의 녹색 */
}

.product-card p {
  margin: 8px 0; /* 문단 간의 간격 */
  color: #333; /* 일반 텍스트 색상 */
}

.toggle-button {
  background-color: #76c7c0; /* 버튼 배경색 */
  color: #fff; /* 버튼 글자색 */
  border: none; /* 테두리 없음 */
  padding: 10px 15px; /* 패딩 */
  border-radius: 5px; /* 둥글게 */
  cursor: pointer; /* 커서 포인터 */
}

.toggle-button:hover {
  background-color: #5ba8a5; /* 버튼 호버 효과 */
}

.product-image {
  width: 300px; /* 카드의 너비에 맞게 이미지 너비 설정 */
  height: 300px; /* 고정된 높이 설정 */
  border-radius: 10px; /* 모서리 둥글게 */
  margin-bottom: 12px; /* 카드 내 이미지와 텍스트 간의 간격 */
  object-fit: cover; /* 이미지 비율을 유지하며 잘리도록 설정 */
}

@media (max-width: 1024px) {
  .product-card {
    flex: 1 1 calc(33.333% - 20px); /* 3개씩 표시 */
  }
}

@media (max-width: 768px) {
  .product-card {
    flex: 1 1 calc(50% - 20px); /* 2개씩 표시 */
  }
}

@media (max-width: 480px) {
  .product-card {
    flex: 1 1 calc(100% - 20px); /* 1개씩 표시 */
  }
}

</style>