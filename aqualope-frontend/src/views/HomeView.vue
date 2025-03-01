<template>
    <MenuHeader />
    <ChartComponent />
</template>

<script>
import MenuHeader from '@/components/MenuHeader.vue';
import ChartComponent from '@/components/ChartComponent.vue';


export default {
  name: "HomeView",
  components: {
    MenuHeader,
    ChartComponent
  },
  setup() {

    return {
    };
  },
};
</script>
