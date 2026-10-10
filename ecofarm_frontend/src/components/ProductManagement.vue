<template>
  <div class="product-management">
    <button @click="showAddForm = !showAddForm" class="button toggle-button">
      {{ showAddForm ? '취소' : '재배 물품 추가' }}
    </button>

    <div v-if="showAddForm" class="add-product-form">
      <h3>물품 추가</h3>
      <form @submit.prevent="submitForm">
        <div class="form-group">
          <label for="name">물품명:</label>
          <input v-model="newProduct.productName" id="name" required />
        </div>
        <div class="form-group">
          <label for="description">설명:</label>
          <input v-model="newProduct.description" id="description" required />
        </div>
        <button type="submit" class="button submit-button">저장</button>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      </form>
    </div>

    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <table v-else class="product-table">
      <thead>
        <tr>
          <th class="productID">물품번호</th>
          <th calss="productName">물품명</th>
          <th>설명</th>
          <th class="edit">정보수정</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="product in products" :key="product.productID">
          <td class="productID">물품번호-{{ product.productID }}</td>
          <td v-if="editingProductID === product.productID">
            <input v-model="product.productName" />
          </td>
          <td v-else>{{ product.productName }}</td>
          
          <td v-if="editingProductID === product.productID">
            <input v-model="product.description" />
          </td>
          <td v-else>{{ product.description }}</td>

          <td class="edit">
            <button v-if="editingProductID === product.productID" @click="saveProduct(product)" class="button edit-button">저장</button>
            <button v-if="editingProductID === product.productID" @click="cancelEdit" class="button cancel-button">취소</button>
            <button v-else @click="editProduct(product.productID)" class="button edit-button">수정</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

  
<script>
export default {
  data() {
    return {
      showAddForm: false, // 물품 추가 폼 표시 여부
      newProduct: {
        productName: '',
        description: ''
      },
      products: [], // 물품 리스트 데이터
      errorMessage: '', // 에러 메시지
      editingProductID: null // 수정 중인 제품의 ID
    };
  },
  methods: {
    async fetchProducts() {
      this.errorMessage = ''; // 초기화
      try {
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch('http://localhost:8080/api/products', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const data = await response.json();
          this.products = data;
        } else if (response.status === 401) {
          this.$store.dispatch('logout');
          alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
          this.$router.push('/login');
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '물품 목록을 가져오는데 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to fetch products:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    async submitForm() {
      this.errorMessage = ''; // 초기화
      try {
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch('http://localhost:8080/api/products', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}` // JWT 토큰 추가
          },
          body: JSON.stringify(this.newProduct)
        });

        if (response.ok) {
          this.showAddForm = false;
          this.newProduct = {
            productName: '',
            description: ''
          };
          this.fetchProducts(); // 새로 추가된 물품을 리스트에 반영
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '물품 추가에 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to add product:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    },
    editProduct(productID) {
      this.editingProductID = productID;
    },
    cancelEdit() {
      this.editingProductID = null;
      this.fetchProducts(); // 수정을 취소하면 원래 데이터로 복원
    },
    async saveProduct(product) {
      this.errorMessage = ''; // 초기화
      try {
        const token = sessionStorage.getItem('jwtToken');
        const response = await fetch('http://localhost:8080/api/products/update', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}` // JWT 토큰 추가
          },
          body: JSON.stringify({
            productID: product.productID,
            productName: product.productName,
            description: product.description
          })
        });

        if (response.ok) {
          this.editingProductID = null;
          this.fetchProducts(); // 업데이트된 물품을 리스트에 반영
        } else {
          const errorData = await response.json();
          this.errorMessage = errorData.message || '물품 정보 수정에 실패했습니다.';
        }
      } catch (error) {
        console.error('Failed to update product:', error);
        this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
      }
    }
  },
  mounted() {
    this.fetchProducts();
  }
};
</script>

<style scoped>
.product-management {
  flex-direction: column;
}

.add-product-form {
  border-radius: 8px; /* 모서리 둥글게 */
  padding: 20px; /* 내부 여백 */
  background-color: #e8f5e9; /* 연한 초록색 배경 */
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  margin-top: 20px; /* 상단 여백 */
}

.add-product-form h3 {
  margin-bottom: 10px;
  color: black; /* 제목 색상 */
}

.add-product-form form {
  flex-direction: column; /* 세로 정렬 */
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: black; /* 라벨 색상 */
}

.form-group input {
  width: 40%; /* 입력 필드 너비 */
  padding: 10px; /* 패딩 */
  border: 1px solid #a5d6a7; /* 연한 초록색 테두리 */
  border-radius: 4px; /* 둥글게 */
  background-color: #fff; /* 흰색 배경 */
}

.submit-button {
  background-color: #66bb6a; /* 버튼 배경색 */
  color: white; /* 글자색 */
  border: none;
  cursor: pointer;
  border-radius: 4px;
  padding: 10px 20px; /* 버튼 내부 여백 */
  transition: background-color 0.3s ease;
}

.submit-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

.error-message {
  color: red; /* 에러 메시지 색상 */
  margin-top: 10px; /* 상단 여백 */
}

.product-table {
  width: 100%;
  table-layout: fixed; /* 고정 레이아웃 */
  border-collapse: collapse; /* 테이블 경계 겹침 해제 */
  margin-top: 20px; 
  border-radius: 10px; /* 모서리 둥글게 */
  overflow: hidden; /* 모서리 둥글게 효과 */
}

.product-table th {
  background-color: #4CAF50; /* 진한 녹색 배경 */
  color: white; /* 흰색 글씨 */
  text-align: center; /* 가운데 정렬 */
  padding: 10px; /* 패딩 */
}

.product-table th,
.product-table td {
  border: 1px solid #c8e6c9; /* 연한 초록색 테두리 */
  padding: 8px;
}

.product-table td {
  background-color: #f1f8e9; /* 연한 초록색 배경 */
  color: #333; /* 일반 텍스트 색상 */
  height: 50px; /* 원하는 높이 설정 */
  vertical-align: middle; /* 수직 정렬을 중간으로 설정 */
}

.product-table td input {
  width: 90%; /* 입력 필드 전체 너비 설정 */
  padding: 8px; /* 패딩 추가 */
  border: 1px solid #c8e6c9; /* 테두리 색상 */
  border-radius: 5px; /* 둥근 모서리 */
}

.product-table td.edit {
  text-align: center;
}

.product-table th.productID,
.product-table td.productID,
.product-table th.edit,
.product-table td.edit {
  width: 140px; /* 원하는 너비 설정 */
}

.toggle-button,
.edit-button,
.cancel-button {
  margin-left: 5px;
  margin-right: 5px; /* 버튼 사이에 5px의 마진 추가 */
  background-color: #66bb6a; /* 버튼 배경색 */
  color: white; /* 글자색 */
  border: none;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.toggle-button:hover,
.edit-button:hover,
.cancel-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

</style>
