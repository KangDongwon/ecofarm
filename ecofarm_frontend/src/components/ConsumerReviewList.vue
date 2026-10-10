<template>
  <div class="review-list-container">
    <h2 class="review-title">작성한 리뷰 목록</h2>
    <ul v-if="reviews.length > 0" class="review-list">
      <li v-for="review in reviews" :key="review.reviewID" class="review-item">
        <div class="review-header">
          <p class="product-name">{{ review.productName }}</p>
          <div class="star-rating">
            <span class="star" v-for="n in 5" :key="n" :class="{ 'filled': n <= review.rating }">★</span>
          </div>
        </div>
        <p class="review-detail review-seller-order">
          <strong>판매자명:</strong> {{ review.sellerName }} | <strong>주문일자:</strong> {{ review.orderDate }}
        </p>
        <p class="review-detail review-comment"> {{ review.comment }}</p>
        <p class="review-date">작성일: {{ review.reviewDate }}</p>
      </li>
    </ul>
    <div v-else class="no-reviews-message">작성한 리뷰가 없습니다.</div>
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  computed: {
    ...mapState(['username'])
  },
  data() {
    return {
      reviews: [],
      errorMessage: ''
    };
  },
  created() {
    this.fetchReviews();
  },
  methods: {
    async fetchReviews() {
      try {
        const email = this.username;
        const response = await fetch(`http://localhost:8080/api/reviews/consumer/${email}`, {
          headers: { 'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}` }
        });

        if (response.ok) {
          let reviews = await response.json();
          this.reviews = reviews.sort((a, b) => new Date(b.reviewDate) - new Date(a.reviewDate));
        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '리뷰 목록을 가져오는 데 실패했습니다.';
        }
      } catch (error) {
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
        this.reviews = [];
      }
    }
  }
};
</script>

<style scoped>
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
  text-align: center;
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