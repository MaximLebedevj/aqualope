import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from 'axios';
import { createPinia } from 'pinia';
import './assets/base.css';

const app = createApp(App);

app.config.globalProperties.$axios = axios;

app.use(createPinia());

app.use(router);

app.mount('#app');
