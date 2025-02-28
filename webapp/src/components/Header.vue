<template>
  <header>
    <div class="logo" @click="goHome">
      <img src="@/assets/logo.svg" alt="Logo" />
    </div>
    <nav>
      <ul>
        <li v-if="!isAuthenticated">
          <router-link to="/login">Login</router-link>
        </li>
        <li v-if="!isAuthenticated">
          <router-link to="/register">Register</router-link>
        </li>
        <li v-if="isAuthenticated">
          <router-link to="/profile">Profile</router-link>
        </li>
      </ul>
    </nav>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const isAuthenticated = computed(() => authStore.isAuthenticated);

const goHome = () => {
  router.push('/');
};
</script>

<style scoped>
header {
  background-color: #333;
  color: white;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

header .logo img {
  width: 50px;
  cursor: pointer;
}

header nav ul {
  display: flex;
  gap: 20px;
  list-style: none;
}

header nav a {
  color: white;
  padding: 10px;
  border-radius: 5px;
}

header nav a:hover {
  background-color: #444;
}
</style>
