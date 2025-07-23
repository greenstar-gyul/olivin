import { defineStore } from 'pinia';
import axios from '@/service/axios';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    permissions: [],
}),
actions: {
  async login(email, password) {
    try {
      const res = await axios.post('/auth/login', { email, password })
      this.user = res.data.name
      this.permissions = res.data.permissions
      this.isAuthenticated = true
      console.log('Login successful:', res.data);
    } catch (err) {
      console.error('Login failed:', err.response ? err.response.data : err.message);
    }
  },
  logout() {
    this.user = null;
    this.isAuthenticated = false;
    this.permissions = [];
    console.log('Logged out successfully');
  },
},
})