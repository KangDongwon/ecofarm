<template>
  <div class="product-detail">
    <div class="title-container">
      <h1>상세보기</h1>
      <!-- 구매하기 버튼 -->
      <button @click="openPurchasePopup" class="purchase-button">구매하기</button>
    </div>
    
    <div v-if="loading">로딩 중...</div>
    <div v-if="errorMessage">{{ errorMessage }}</div>
    <div v-if="product">
      <p><strong>상품명:</strong> {{ product.productName }}</p>
      <p><strong>판매자명:</strong> {{ product.sellerName }}
        <span class="average-rating">
          <span v-for="star in Math.floor(averageRating)" :key="star" class="filled-star">★</span>
          <span v-if="averageRating % 1 !== 0" class="half-star">☆</span> <!-- 반별점 표시 -->
          <span class="average-rating-num"> ({{ averageRating }})</span>
        </span>
      </p>
      <p><strong>가격:</strong> {{ formatPrice(product.price) }}원</p>
      <p><strong>남은수량:</strong> {{ product.quantity }}</p>
      <p><strong>유효 기간:</strong> {{ product.availableUntilDate }}</p>
      <p><strong>물품 영상:</strong></p>
      <iframe
        :src="getVideoUrl(product.youTubeLink)"
        frameborder="0"
        allowfullscreen
        class="youtube-iframe"
      ></iframe>

      <!-- 필터링된 리뷰 정보 -->
      <div>
        <h2 class="review-title">리뷰 목록</h2>
        <ul v-if="reviews.length > 0" class="review-list">
          <li v-for="review in reviews" :key="review.reviewID" class="review-item">
            <div class="review-header">
              <p class="product-name">{{ review.productName }}</p>
              <div class="star-rating">
                <span class="star" v-for="n in 5" :key="n" :class="{ 'filled': n <= review.rating }">★</span>
              </div>
            </div>
            <p class="review-detail review-seller-order">
              <strong>구매자명:</strong> {{ maskConsumerName(review.consumerName) }} | <strong>주문일자:</strong> {{ review.orderDate }}
            </p>
            <p class="review-detail review-comment">{{ review.comment }}</p>
            <p class="review-date">작성일: {{ review.reviewDate }}</p>
          </li>
        </ul>
        <div v-else class="no-reviews-message">작성한 리뷰가 없습니다.</div>
      </div>
    </div>

    <!-- 구매하기 팝업 -->
    <div v-if="showPopup" class="popup-overlay">
  <div class="popup-content">
    <p class="popup-label"><strong>최대 구매 수량:</strong> {{ product.quantity }}</p>
    <input type="number" v-model="purchaseQuantity" :max="product.quantity" min="1" @input="updatePurchaseQuantity" class="quantity-input" />
    <p class="popup-price"><strong>총 가격:</strong> {{ formatPrice(totalPrice) }}원</p>
    <div class="popup-buttons">
      <button @click="purchaseProduct" class="popup-button confirm">구매하기</button>
      <button @click="closePopup" class="popup-button cancel">취소</button>
    </div>
  </div>
</div>

  </div>
</template>

<script>
export default {
  props: ['saleProduct', 'changeSection'],
  data() {
    return {
      product: null,
      reviews: [],
      averageRating: 0,
      loading: false,
      errorMessage: null,
      showPopup: false,
      purchaseQuantity: 1,
      totalPrice: 0
    };
  },
  created() {
    this.product = this.saleProduct;
    //console.log('here');
    //console.table(this.saleProduct);
    //this.fetchProductDetails();
    this.fetchSellerReivews();
  },
  methods: {
    maskConsumerName(name) {
      if (name.length < 2) return name; // 이름이 1글자일 경우 그대로 반환
      return name[0] + '*'.repeat(name.length - 1); // 첫 글자는 그대로, 나머지는 '*'
    },
    async fetchSellerReivews() {
      this.loading = true;
      try {
        //console.log(sessionStorage.getItem('jwtToken'));
        console.log('here ' + this.product.sellerID);
        const response = await fetch(`http://localhost:8080/api/reviews/seller/${this.product.sellerID}`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}`,
            'Content-Type': 'application/json'
          }
        });
        if (response.ok) {
          let reviews = await response.json();
          console.table(reviews);

          // 평균 별점 계산
          const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0);
          const averageRating = reviews.length > 0 ? (totalRating / reviews.length).toFixed(2) : 0;
          console.log('Average Rating:', averageRating);
          this.averageRating = averageRating; // 별점 평균을 저장

          // 필터링: saleProductID로 필터링
          //reviews = reviews.filter(review => review.saleProductID === this.product.saleProductID);

          // 정렬: reviewDate로 정렬 (오름차순 또는 내림차순)
          reviews = reviews.sort((a, b) => new Date(b.reviewDate) - new Date(a.reviewDate)); // 내림차순
          // reviews = reviews.sort((a, b) => new Date(a.reviewDate) - new Date(b.reviewDate)); // 오름차순

          // 계산된 리뷰를 this.reviews에 저장
          this.reviews = reviews;

        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '리뷰 정보를 가져오는데 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to fetch sale products:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
        this.product = [];
      } finally {
        this.loading = false;
      }
    },
    async fetchProductDetails() {
      this.loading = true;
      try {
        //console.log(sessionStorage.getItem('jwtToken'));
        //console.log('here ' + this.saleProductID);
        const response = await fetch(`http://localhost:8080/api/saleproducts/${this.saleProductID}`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}`,
            'Content-Type': 'application/json'
          }
        });
        if (response.ok) {
          this.product = await response.json();
          this.totalPrice = this.product.price;
        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '물품 상세정보를 가져오는데 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to fetch sale products:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
        this.product = [];
      } finally {
        this.loading = false;
      }
    },
    formatPrice(value) {
      return new Intl.NumberFormat().format(value);
    },
    openPurchasePopup() {
      this.showPopup = true;
      this.purchaseQuantity = 1;
      this.updateTotalPrice();
    },
    closePopup() {
      this.showPopup = false;
    },
    updatePurchaseQuantity() {
      if (this.purchaseQuantity > this.product.quantity) {
        alert("구매 가능 수량이 초과되었습니다.");
        this.purchaseQuantity = this.product.quantity; // 수량을 최대값으로 설정
      }
      this.updateTotalPrice();
    },
    updateTotalPrice() {
      this.totalPrice = this.purchaseQuantity * this.product.price;
    },
    async purchaseProduct() {
      const email = this.$store.state.username; // Vuex에서 email 가져오기
      const token = sessionStorage.getItem('jwtToken'); // 로컬 스토리지에서 토큰 가져오기
      
      const purchaseData = {
        saleProductID: this.product.saleProductID,
        purchaseQuantity: this.purchaseQuantity,
        totalPrice: this.totalPrice,
        consumerEmail: email // 이메일 추가
      };

      try {
        const response = await fetch(`http://localhost:8080/api/customerorders/purchase`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(purchaseData)
        });

        if (response.ok) {
          alert(`구매 완료: 수량 ${this.purchaseQuantity}, 총 가격 ${this.totalPrice} 원`);
          this.closePopup();
          this.changeSection('order');
        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        } else {
          const errorData = await response.json();
          alert(errorData.message || '구매에 실패했습니다.');
        }
      } catch (error) {
        console.error('구매 요청 실패:', error);
        alert('서버와의 연결에 문제가 발생했습니다.');
      }
    },
    getVideoUrl(url) {
      const videoId = this.getVideoId(url);
      return videoId ? `https://www.youtube.com/embed/${videoId}` : '';
    },
    getVideoId(url) {
      const regExp = /^.*((youtu.be\/)|(v\/)|(u\/w\/)|(embed\/)|(watch\?v=))([^&\n]{11})/;
      const match = url.match(regExp);
      return match && match[7] ? match[7] : null;
    }
  }
};
</script>

<style scoped>
.product-detail {
  margin: 20px;
}

.title-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.purchase-button {
  background-color: #28a745;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.purchase-button:hover {
  background-color: #218838;
}

.youtube-iframe {
  width: 70%;
  height: 392px;
  border: none;
}

.popup-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7); /* 어두운 반투명 배경 */
  z-index: 1000; /* 최상위 레이어 */
}

.popup-content {
  background-color: white;
  padding: 20px;
  border-radius: 5px;
  width: 300px;
  text-align: center;
}

.popup-label {
  font-size: 16px; /* 라벨 크기 */
  margin-bottom: 10px;
  color: #333; /* 라벨 색상 */
}

.popup-buttons {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.quantity-input {
  text-align: center;
  margin-top: 40px;
  width: 50%; /* 입력창 전체 너비 */
  padding: 5px; /* 패딩을 증가시켜 입력이 더 편하게 */
  font-size: 18px; /* 입력창 폰트 크기 증가 */
  border: 2px solid #ccc; /* 경계선 두께 증가 */
  border-radius: 5px; /* 경계선 둥글게 */
  margin-bottom: 20px; /* 여백 */
  box-sizing: border-box; /* 패딩과 경계선 포함하여 전체 너비 계산 */
}

.quantity-input::-webkit-inner-spin-button,
.quantity-input::-webkit-outer-spin-button {
    height: 30px; /* 높이 조정 */
    width: 30px; /* 너비 조정 */
    font-size: 20px; /* 글꼴 크기 조정 */
    transform: scale(2); /* 크기 증가 */
}

.quantity-input {
  height: 50px; /* 입력 필드 높이 설정 */
  font-size: 20px; /* 폰트 크기 조정 */
}

.popup-price {
  font-size: 20px; /* 가격 크기 */
  font-weight: bold; /* 두꺼운 글씨 */
  color: #2c6e49; /* 가격 색상 */
  margin-bottom: 50px; /* 여백 */
}

.popup-button {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
}

.popup-button.confirm {
  background-color: #007bff;
  color: white;
}

.popup-button.cancel {
  background-color: #dc3545;
  color: white;
}

.popup-button.confirm:hover {
  background-color: #0056b3;
}

.popup-button.cancel:hover {
  background-color: #c82333;
}

.average-rating {
  margin-left: 10px; /* 판매자명과 간격 조정 */
}

.filled-star, .half-star {
  font-size: 24px; /* 별의 크기 조정 (필요에 따라 조정 가능) */
  color: #ffcc00; /* 채워진 별 색상 */
}

.half-star {
  position: relative; /* 부모 요소의 위치를 기준으로 자식 요소 위치 설정 */
  color: transparent; /* 별의 기본 색상은 투명으로 설정 */
}

.half-star:before {
  content: '★'; /* 별의 기호 */
  position: absolute;
  left: 0; /* 왼쪽에 위치 */
  width: 50%; /* 50% 너비를 차지하게 하여 반만 채워진 것처럼 보이게 함 */
  overflow: hidden; /* 넘치는 부분 숨김 */
  color: #ffcc00; /* 반별점 색상 */
}

.average-rating-num {
  font-size: 18px; /* 평점 숫자의 크기 */
  color: #333; /* 평점 숫자의 색상 */
}

.review-list-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #e7f3e6; /* 부드러운 연두색 배경 */
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.review-title {
  font-size: 24px;
  font-weight: bold;
  text-align: start;
  margin-bottom: 20px;
  color: #2c6e49; /* 제목 색상 */
}

.review-list {
  list-style-type: none;
  padding: 0;
}

.review-item {
  background-color: rgba(255, 255, 255, 0.9); /* 약간 투명한 흰색 배경 */
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.review-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2); /* 강조 효과 */
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  font-size: 20px;
  font-weight: bold;
  color: #2e5c4e; /* 상품명 색상 */
}

.star-rating {
  display: flex;
}

.star {
  font-size: 20px;
  color: #ddd;
  margin-left: 5px;
  transition: color 0.2s;
}

.star.filled {
  color: #ffcc00;
}

.review-detail {
  font-size: 14px;
  margin: 5px 0;
  color: #444; /* 리뷰 내용 색상 */
}

.review-seller-order {
  font-size: 14px;
  color: #444; /* 동일한 색상 유지 */
  margin-top: 0px;
  margin-bottom: 30px; /* 여백 조정 */
}

.review-comment {
  font-size: 18px; /* 리뷰 내용 크기 증가 */
  font-weight: bold; /* 두껍게 */
  color: #3b8b3a; /* 강조된 리뷰 내용 색상 */
}

.review-date {
  font-size: 12px;
  color: #666;
  text-align: right;
  margin-top: 10px;
}

.no-reviews-message {
  text-align: center;
  font-size: 18px;
  color: #333;
  margin-top: 20px;
}

</style>
