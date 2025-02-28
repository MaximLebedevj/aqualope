<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from "../stores/auth";
import Header from "../components/Header.vue";

const authStore = useAuthStore();
const loginData = ref({ username: "", password: ""});
const router = useRouter();

const login = async () => {
  try {
    const response = await axios.post('/api/login', loginData.value);
    authStore.setToken(response.data.token);
    await router.push('/');
  } catch (error) {
    alert('Login failed');
  }
};
</script>

<template>
  <div>
    <Header />
    <h1>Login</h1>
    <input v-model="loginData.username" placeholder="Username" />
    <input v-model="loginData.password" type="password" placeholder="Password" />
    <button @click="login">Login</button>
  </div>
</template>


