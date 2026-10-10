<template>
    <div class="main-page">
        <div class="content">
            <img class="main-image" src="../assets/community1.png" alt="메인 이미지" />
            <img class="main-image" src="../assets/community2.png" alt="메인 이미지" />
        </div>
        <footer class="footer">
            <p>© 2024 EcoFarm Co., Ltd. All rights reserved.</p>
        </footer>
    </div>
</template>
  
<script>
  export default {
    name: 'ChronicDiseasePrevention',
  };
</script>
  
<style scoped>
  .main-page {
    display: flex;
    flex-direction: column;
    height: 100vh; /* 뷰포트 높이 100% 설정 */
  }
  
  .content {
    margin-top: 10px;
    flex-direction: column;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgb(245, 245, 217);
  }
  
  .main-image {
    width: 80%;
    height: 100%;
    object-fit: cover;
    margin-bottom: 50px;
    margin-top: 50px;
  }
  
  .footer {
    margin-top: 10px;
    text-align: center;
    padding: 10px;
    background-color: #f1f1f1;
    font-size: 0.9rem;
    color: #555;
  }
  </style>
  