import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
    state: () => {
        return {
            token: localStorage.getItem('token') || '',
        };
    },
    actions: {
        setToken(token) {
            this.token = token;
            localStorage.setItem('token', token);
        },
        removeToken() {
            this.token = '';
            localStorage.removeItem('token');
        },
    },
    getters: {
        isAuthenticated: (state) => {
            return !!state.token;
        },
    },
});
