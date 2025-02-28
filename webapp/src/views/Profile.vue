<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useAuthStore } from "../stores/auth";
import Header from "../components/Header.vue";

const authStore = useAuthStore();

const userInfo = ref({});
const router = useRouter();

const getUserInfo = async () => {
  const token = localStorage.getItem("token");
  try {
    const response = await axios.get('/api/user/info', {
      headers: { Authorization: `Bearer ${token}` },
    });
    userInfo.value = response.data;
  } catch (error) {
    alert('Failed to fetch user info');
  }
};

const logout = () => {
    authStore.removeToken();
    router.push('/');
}

onMounted(() => {
    getUserInfo();
});
</script>

<template>
    <div>
      <Header />
        <h1>User Profile</h1>
        <p>Username: {{ userInfo.username }}</p>
        <button @click="logout">Logout</button>
    </div>
</template>