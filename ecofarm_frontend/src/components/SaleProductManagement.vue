<template>
  <div class="saleproduct-management">
    <button @click="showAddForm = !showAddForm" class="toggle-button">
      {{ showAddForm ? '취소' : '물품 추가' }}
    </button>

    <div v-if="showAddForm" class="add-product-form">
      <h3>판매 물품 추가</h3>
      <form @submit.prevent="submitForm">
        <div class="form-group">
          <label for="productName">판매 물품명:</label>
          <select v-model="newProduct.productID" id="productName" required>
            <option v-for="product in productOptions" :key="product.productID" :value="product.productID">
              {{ product.productName }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="availableUntilDate">유효기간:</label>
          <input type="date" v-model="newProduct.availableUntilDate" id="availableUntilDate" required :min="minAvailableDate" />
        </div>
        <div class="form-group">
          <label for="quantity">수량:</label>
          <input type="number" v-model="newProduct.quantity" id="quantity" required />
        </div>
        <div class="form-group">
          <label for="price">가격:</label>
          <input type="number" v-model="newProduct.price" id="price" required />
        </div>
        <div class="form-group">
          <label for="youTubeLink">유튜브 링크:</label>
          <input v-model="newProduct.youTubeLink" id="youTubeLink" />
        </div>
        <button type="submit" class="submit-button">저장</button>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      </form>
    </div>

    <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

    <div v-if="products.length === 0" class="no-products-message">
      등록된 물품이 없습니다.
    </div>

    <table v-else class="product-table">
      <thead>
        <tr>
          <th>판매 물품명</th>
          <th>판매자명</th>
          <th>유효기간</th>
          <th>수량</th>
          <th>가격</th>
          <th>유튜브 링크</th>
          <th>승인 상태</th>
          <th>승인일자</th>
          <th class="mod">수정</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(product, index) in products" :key="product.saleProductID">
          <td>
            <template v-if="editingIndex === index && product.approvalStatus !== '승인'">
              <select v-model="editableProduct.productID">
                <option v-for="option in productOptions" :key="option.productID" :value="option.productID">
                  {{ option.productName }}
                </option>
              </select>
            </template>
            <template v-else>
              {{ product.productName }}
            </template>
          </td>
          <td>
            <template v-if="editingIndex !== index">
              {{ product.sellerName }}
            </template>
            <template v-else>
              <input v-model="editableProduct.sellerName" disabled />
            </template>
          </td>
          <td>
            <template v-if="editingIndex === index && product.approvalStatus !== '승인'">
              <input type="date" v-model="editableProduct.availableUntilDate" :min="minAvailableDate" />
            </template>
            <template v-else>
              {{ product.availableUntilDate }}
            </template>
          </td>
          <td>
            <template v-if="editingIndex === index && product.approvalStatus !== '승인'">
              <input type="number" v-model="editableProduct.quantity" />
            </template>
            <template v-else>
              {{ product.quantity }}
            </template>
          </td>
          <td>
            <template v-if="editingIndex === index && product.approvalStatus !== '승인'">
              <input type="number" v-model="editableProduct.price" />
            </template>
            <template v-else>
              {{ formatPrice(product.price) }}
            </template>
          </td>
          <td>
            <template v-if="editingIndex === index && product.approvalStatus !== '승인'">
              <input v-model="editableProduct.youTubeLink" />
            </template>
            <template v-else>
              <a :href="product.youTubeLink" target="_blank" v-if="product.youTubeLink">동영상 링크</a>
              <span v-else></span>
            </template>
          </td>
          <td>{{ product.approvalStatus }}</td>
          <td>{{ product.approvalDate }}</td>
          <td class="mod">
            <template v-if="editingIndex === index">
              <div class="action-buttons">
                <button @click="saveProduct(product.saleProductID)" class="submit-button edit-button">저장</button>
                <button @click="cancelEdit" class="toggle-button edit-button">취소</button>
              </div>
            </template>
            <template v-else>
              <button v-if="product.approvalStatus !== '승인'" @click="editProduct(index, product)" class="toggle-button">수정</button>
            </template>
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
          showAddForm: false,
          newProduct: {
            productID: null,
            availableUntilDate: '',
            quantity: 0,
            price: 0,
            youTubeLink: ''
          },
          products: [],
          productOptions: [],
          editingIndex: null,
          editableProduct: {},
          errorMessage: '',
          minAvailableDate: new Date().toISOString().split('T')[0]
        };
      },
      methods: {
        formatPrice(value) {
          return new Intl.NumberFormat('ko-KR').format(value); // 한국식 천 단위 콤마 처리
        },
        async fetchProducts() {
          this.errorMessage = '';
          try {
            const token = sessionStorage.getItem('jwtToken');
            const email = this.$store.state.username;
            const response = await fetch(`http://localhost:8080/api/saleproducts/by-seller?email=${encodeURIComponent(email)}`, {
              headers: {
                'Authorization': `Bearer ${token}`
              }
            });
    
            if (response.ok) {
              const data = await response.json();
              this.products = data.sort((a, b) => {
                const dateA = new Date(a.availableUntilDate);
                const dateB = new Date(b.availableUntilDate);
                return dateB - dateA; 
              });
            } else if (response.status === 401) {
              this.$store.dispatch('logout');
              alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
              this.$router.push('/login');
            } else {
              const errorData = await response.json();
              this.errorMessage = errorData.message || '판매 물품 목록을 가져오는데 실패했습니다.';
            }
          } catch (error) {
            console.error('Failed to fetch saleproducts:', error);
            this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
            this.products = [];
          }
        },
        async fetchProductOptions() {
          this.errorMessage = '';
          try {
            const token = sessionStorage.getItem('jwtToken');
            const response = await fetch('http://localhost:8080/api/products', {
              headers: {
                'Authorization': `Bearer ${token}`
              }
            });
            if (response.ok) {
              this.productOptions = await response.json();
            } else if (response.status === 401) {
              this.$store.dispatch('logout');
              alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
              this.$router.push('/login');
            } else {
              const errorData = await response.json();
              this.errorMessage = errorData.message || '물품 목록을 가져오는데 실패했습니다.';
            }
          } catch (error) {
            console.error('Failed to fetch product options:', error);
            this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
          }
        },
        async submitForm() {
          this.errorMessage = '';
          try {
            const email = this.$store.state.username;
            const productWithEmail = {
              sellerEmail: email,
              ...this.newProduct
            };
            const token = sessionStorage.getItem('jwtToken');
            const response = await fetch('http://localhost:8080/api/saleproducts/add', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
              },
              body: JSON.stringify(productWithEmail)
            });
            if (response.ok) {
              alert('상품이 성공적으로 추가되었습니다.');
              this.fetchProducts();
              this.showAddForm = false;
              this.newProduct = {
                productID: null,
                availableUntilDate: '',
                quantity: 0,
                price: 0,
                youTubeLink: ''
              };
            } else if (response.status === 401) {
              this.$store.dispatch('logout');
              alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
              this.$router.push('/login');
            } else {
              const errorData = await response.json();
              this.errorMessage = errorData.message || '상품을 추가하는데 실패했습니다.';
            }
          } catch (error) {
            console.error('Failed to submit product:', error);
            this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
          }
        },
        editProduct(index, product) {
          this.editingIndex = index;
          this.editableProduct = { ...product };
          // productName으로 productID를 설정
          const selectedOption = this.productOptions.find(option => option.productName === product.productName);
          if (selectedOption) {
            this.editableProduct.productID = selectedOption.productID;
          }
        },
        async saveProduct(saleProductID) {
          this.errorMessage = '';
          try {
            const token = sessionStorage.getItem('jwtToken');
            // 필수 필드만 포함되도록 객체 생성
            const updatePayload = {
              sellerEmail: this.$store.state.username,
              productID: this.editableProduct.productID,
              availableUntilDate: this.editableProduct.availableUntilDate,
              quantity: this.editableProduct.quantity,
              price: this.editableProduct.price,
              youTubeLink: this.editableProduct.youTubeLink
            };

            const response = await fetch(`http://localhost:8080/api/saleproducts/update/${saleProductID}`, {
              method: 'PUT',
              headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
              },
              body: JSON.stringify(updatePayload)
            });
            if (response.ok) {
              alert('상품이 성공적으로 업데이트되었습니다.');
              this.fetchProducts();
              this.editingIndex = null;
              this.editableProduct = {};
            } else if (response.status === 401) {
              this.$store.dispatch('logout');
              alert('세션이 만료되었습니다. 다시 로그인해 주세요.');
              this.$router.push('/login');
            } else {
              const errorData = await response.json();
              this.errorMessage = errorData.message || '상품을 업데이트하는데 실패했습니다.';
            }
          } catch (error) {
            console.error('Failed to update product:', error);
            this.errorMessage = '서버와의 연결에 문제가 발생했습니다.';
          }
        },
        cancelEdit() {
          this.editingIndex = null;
          this.editableProduct = {};
        }
      },
      created() {
        this.fetchProducts();
        this.fetchProductOptions();
      }
    };
</script>
  
<style scoped>
.saleproduct-management {
  flex-direction: column;
}

.add-product-form {
  border-radius: 8px; /* 모서리 둥글게 */
  padding: 20px; /* 내부 여백 */
}

.add-product-form h3 {
  margin-bottom: 10px;
  color: black; /* 제목 색상 */
}

.add-product-form form {
  flex-direction: column;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: black;
}

.form-group input,
.form-group select {
  width: 40%;
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
  color: red;
  margin-top: 10px;
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

.product-table td input,
.product-table td select {
  width: 90%; /* 입력 필드 전체 너비 설정 */
  padding: 8px; /* 패딩 추가 */
  border: 1px solid #c8e6c9; /* 테두리 색상 */
  border-radius: 5px; /* 둥근 모서리 */
}

.toggle-button, .submit-button {
  margin-left: 5px;
  margin-right: 5px;
  display: inline-block; /* 내용에 맞게 크기 조정 */
  width: auto;
  padding: 8px 16px;
  background-color: #66bb6a; /* 버튼 배경색 */
  color: white;
  border: none;
  cursor: pointer;
  font-size: 14px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.toggle-button:hover, .submit-button:hover {
  background-color: #388e3c; /* 호버 색상 */
}

.no-products-message {
  text-align: center; /* 가운데 정렬 */
  font-size: 16px; /* 글자 크기 */
  color: #666; /* 회색 글씨 */
}

.mod {
  text-align: center; /* 가운데 정렬 */
}

.product-table td:nth-child(3), /* 주문일자 */
.product-table td:nth-child(4), /* 수량 */
.product-table td:nth-child(5),
.product-table td:nth-child(8) { /* 가격 */
  text-align: right; /* 오른쪽 정렬 */
}
</style>