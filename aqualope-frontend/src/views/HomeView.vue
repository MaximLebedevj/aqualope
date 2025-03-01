<template>
    <MenuHeader />
    <div>
    <label for="dropdown">Выберите вариант:</label>
    <select v-model="selectedOption" @change="handleChange">
      <option disabled value="">Пожалуйста, выберите один из вариантов</option>
      <option v-for="item in options" :key="item.id" :value="item.value">
        {{ item.text }}
      </option>
    </select>
    <p>Выбранный вариант: {{ selectedOption }}</p>
  </div>
</template>

<script>
import MenuHeader from '@/components/MenuHeader.vue';
import { ref, onMounted } from 'vue';
import axios from 'axios';


export default {
  name: "HomeView",
  components: {
    MenuHeader
  },
  setup() {
    const options = ref(['Аквариум 1', 'Аквариум 2', 'Аквариум 3']); // массив для хранения опций
    const selectedOption = ref('Не выбрано'); // выбранная опция

    const fetchOptions = async () => {
      try {
        const response = await axios.get('https://your-api-endpoint.com/options'); // замените на реальный URL API
        options.value = response.data; // сохраняем полученные опции в массив
      } catch (error) {
        console.error(error);
      }
    };

    const handleChange = () => {
      console.log(`Вы выбрали: ${selectedOption.value}`);
      // здесь можно обработать изменение выбранной опции
    };

    // onMounted(async () => {
    //   await fetchOptions(); // загрузка данных при монтировании компонента
    // });

    return {
      options,
      selectedOption,
      // handleChange,
    };
  },
};
</script>
