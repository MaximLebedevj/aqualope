<template>
    <MenuHeader />
    <ParamsComponent />
    <ChartComponent />
</template>

<script>
import MenuHeader from '@/components/MenuHeader.vue';
import ChartComponent from '@/components/ChartComponent.vue';
import ParamsComponent from '@/components/ParamsComponent.vue';


export default {
  name: "HomeView",
  components: {
    MenuHeader,
    ParamsComponent,
    ChartComponent
  },
  setup() {

    return {
    };
  },
};
</script>
