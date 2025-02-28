<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import Header from "../components/Header.vue";

const registerData = ref( { username: "", password: "" });
const router = useRouter();

const register = async () => {
  try {
    await axios.post('/api/register', registerData.value);
    alert('Registration successful');
    await router.push('/login');
  } catch (error) {
    alert('Registration failed');
  }
};
</script>

<template>
  <div>
    <Header />
    <h1>Register</h1>
    <input v-model="registerData.username" placeholder="Username" />
    <input v-model="registerData.password" type="password" placeholder="Password" />
    <button @click="register">Register</button>
  </div>
</template>


