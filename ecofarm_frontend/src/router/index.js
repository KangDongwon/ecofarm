import { createRouter, createWebHistory } from 'vue-router';
import store from '../store';
import MainPage from '../components/MainPage.vue';
import LoginPage from '../components/LoginPage.vue'; 
import SignUpPage from '../components/SignUpPage.vue'; 
import AdminPage from '../components/AdminPage.vue'; 
import ConsumerPage from '../components/ConsumerPage.vue'; 
import SellerPage from '../components/SellerPage.vue';
import ChronicDiseasePrevention from '../components/ChronicDiseasePrevention.vue';
import ApartFarm from '../components/ApartFarm.vue';
import CommunityClub from '../components/CommunityClub.vue';
import DefaultLayout from '../components/DefaultLayout.vue';

const routes = [
  { path: '/', component: MainPage },
  { path: '/',
    component: DefaultLayout,
    children: [
      { path: 'chronic-disease-prevention', component: ChronicDiseasePrevention },
      { path: 'apart-farm', component: ApartFarm },
      { path: 'community-club', component: CommunityClub },
    ],
  },
  { path: '/login', component: LoginPage },
  { path: '/signup', component: SignUpPage },
  { path: '/consumer', component: ConsumerPage,
    meta: { 
      requiresAuth: true, 
      requiredRole: 'ROLE_CONSUMER' 
    }
  },
  { path: '/seller', component: SellerPage,
    meta: { 
      requiresAuth: true, 
      requiredRole: 'ROLE_SELLER' 
    }
  },
  { path: '/admin', component: AdminPage, 
    meta: { 
      requiresAuth: true, 
      requiredRole: 'ROLE_ADMIN' 
    } 
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Navigation Guard for Authentication
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!store.state.token;
  const userRole = store.state.userRole;

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login'); // 로그인 안 되어있으면 로그인 페이지로 리다이렉트
  } else if (to.meta.requiredRole && to.meta.requiredRole !== userRole) {
    next('/'); // 권한이 없으면 홈으로 리다이렉트
  } else {
    next(); // 허용된 경우, 이동
  }
});

export default router;
