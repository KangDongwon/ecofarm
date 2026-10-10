<template>
  <div class="review-container">
    <h2 class="review-title">리뷰 작성</h2>
    
    <div class="order-info">
      <p><strong>물품명:</strong> {{ order.productName }}</p>
      <p><strong>판매자명:</strong> {{ order.sellerName }}</p>
      <p><strong>주문일자:</strong> {{ order.orderDate }}</p>
      <p><strong>주문수량:</strong> {{ order.quantity }}</p>
      <p><strong>가격:</strong> {{ formatPrice(order.totalPrice) }}</p>
    </div>

    <div class="star-rating">
      <span class="star" 
            v-for="n in 5" 
            :key="n" 
            :class="{ 'filled': n <= rating }" 
            @click="setRating(n)">
        ★
      </span>
    </div>      

    <div class="comment-section">
      <textarea id="comment" v-model="comment" placeholder="코멘트를 입력하세요..." rows="4"></textarea>
    </div>

    <button @click="submitReview" class="submit-button">리뷰 제출</button>
  </div>
</template>

<script>
export default {
  props: ['order', 'changeSection'],
  data() {
    return {
      rating: 0,
      comment: ''
    };
  },
  methods: {
    setRating(n) {
      this.rating = n;
    },
    submitReview() {
      fetch('http://localhost:8080/api/reviews', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${sessionStorage.getItem('jwtToken')}`
          },
          body: JSON.stringify({
              orderId: this.order.orderID,
              rating: this.rating,
              comment: this.comment
          })
      })
      .then(response => {
          if (response.ok) {
              return response.text();
          } else {
              throw new Error('리뷰 제출 중 오류가 발생했습니다.');
          }
      })
      .then(data => {
          alert('리뷰가 성공적으로 제출되었습니다.');
          this.changeSection('order');
      })
      .catch(error => {
          alert(error.message);
      });
    },
    formatPrice(value) {
      return new Intl.NumberFormat().format(value);
    },
  }
};
</script>

<style scoped>
.review-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #e7f6e9; /* 연한 초록색 배경 */
}

.review-title {
  font-size: 24px;
  margin-bottom: 20px;
  text-align: center;
  color: #4CAF50; /* 제목 색상 */
}

.order-info {
  margin-bottom: 20px;
  border-bottom: 1px solid #ccc;
  padding-bottom: 10px;
  color: #555; /* 일반 텍스트 색상 */
}

.star-rating {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.star {
  font-size: 30px;
  cursor: pointer;
  color: #ccc;
  transition: color 0.2s;
}

.star.filled {
  color: #ffcc00; /* 선택된 별 색상 */
}

.comment-section {
  margin-bottom: 20px;
}

textarea {
  width: calc(100% - 20px);
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  resize: none;
  box-sizing: border-box;
  background-color: #fff; /* 흰색 배경 */
  color: #333; /* 텍스트 색상 */
}

.submit-button {
  background-color: #4CAF50; /* 초록색 버튼 */
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.submit-button:hover {
  background-color: #45a049; /* 버튼 호버 색상 */
}
</style>
