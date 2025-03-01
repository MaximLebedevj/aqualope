<template>
    <header class="shadow-sm head d-flex flex-wrap align-items-center justify-content-center justify-content-md-between py-3 mb-4 border-bottom">

      <ul class="nav col text-start">
        <li><button type="button" class="btn btn-primary ms-2 nav-button" @click="homePush">Home</button></li>
        <li><button type="button" class="btn btn-primary ms-2 nav-button" @click="monitoringPush">Monitoring</button></li>
      </ul>

      <div class="col text-center">
          <h2 style="color: #000046; font-family: 'Georgia';">Aqualope</h2>
      </div>
      <div class="col text-end">
        <button type="button" class="btn btn-primary me-2 nav-button">Profile</button>
        <button type="button" class="btn btn-primary me-2 nav-button" @click="LoginPush">Login</button>
      </div>
    </header>
      
</template>
  
<script>
import { defineComponent } from 'vue';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'MenuHeader',
  setup() {
    const router = useRouter();
    const loginPush = () => router.push('/login');
    const homePush = () => router.push('/');

    return {
      loginPush,
      homePush,
    };
  },
});
</script>
  
  <!-- Add "scoped" attribute to limit CSS to this component only -->
  <style scoped>
 .head {
  background-color: #01B0F1
 }

 .nav-button {
  background-color: #000046;
 }
  </style>