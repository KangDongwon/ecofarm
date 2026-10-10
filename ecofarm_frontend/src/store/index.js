// src/store/index.js
import { createStore } from 'vuex';

export default createStore({
  state() {
    return {
      userRole: null,
      token: sessionStorage.getItem('jwtToken') || null,
      username: null,
      error: null 
    };
  },
  mutations: {
    setUserRole(state, role) {
      state.userRole = role;
    },
    setToken(state, token) {
      state.token = token;
    },
    setUsername(state, username) {
      state.username = username;
    },
    setError(state, error) {
      state.error = error; 
    },
    clearUserData(state) {
      state.userRole = null;
      state.token = null;
      state.username = null;
      state.error = null; 
    }
  },
  actions: {
    async login({ commit }, { email, password }) {
      try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const errorData = await response.json();
            commit('setError', errorData.message || 'Login failed');
            return false; // 실패 시 false 반환
        }

        const data = await response.json();
        const token = data.token;
        console.log(token);
        const decodedToken = parseJwt(token);
        const role = decodedToken.role;
        const username = decodedToken.sub;
        const name = data.name;
        
        commit('setToken', token);
        commit('setUserRole', role);
        commit('setUsername', username);
        commit('setError', null); // 로그인 성공 시 오류 초기화
        sessionStorage.setItem('jwtToken', token);
        sessionStorage.setItem('name', name);
        return true; // 성공 시 true 반환
      } catch (error) {
        console.error('Login error:', error);
        if (error.name === 'TypeError' && error.message.includes('NetworkError')) {
          // 네트워크 오류일 때
          commit('setError', 'Network error: Please check your internet connection.');
        } else {
          // 기타 오류
          commit('setError', 'An unexpected error occurred.');
        }
        return false; // 오류 발생 시 false 반환
      }
    },
    logout({ commit }) {
        commit('clearUserData');
        commit('clearError');
        sessionStorage.removeItem('jwtToken');
        sessionStorage.removeItem('name');
    },
    initialize({ commit }) {
      const token = sessionStorage.getItem('jwtToken');
      if (token) {
        const decodedToken = parseJwt(token);
        const role = decodedToken.role;
        const username = decodedToken.sub;

        commit('setToken', token);
        commit('setUserRole', role);
        commit('setUsername', username);
      }
    }
  }
});

function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error('Failed to parse JWT token:', e);
    return {};
  }
}
