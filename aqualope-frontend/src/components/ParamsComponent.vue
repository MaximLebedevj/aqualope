<template>
  <div class="fetish">

    <div class="fetish-col">

      <select class="dropdown" v-model="selectedAquarium">
        <option value="" disabled>Выберите аквариум:</option>
        <option v-for="option in aquariumOptions" :key="option.value" :value="option.value">
          {{ option.text }}
        </option>
      </select>

      <select class="dropdown" v-model="selectedMetric">
        <option value="" disabled>Выберите показатель:</option>
        <option v-for="option in metricOptions" :key="option.value" :value="option.value">
          {{ option.text }}
        </option>
      </select>

    </div>

    <div class="fetish-col">

      <p class="input-label">Верхний предел</p>
      <div class="input-field">
          <input type="text" placeholder="Верхний прeдел">
      </div>
      
      <p class="input-label">Нижний предел</p>
      <div class="input-field">
          <input type="text" placeholder="Нижний предел">
      </div>
      
    </div>
  
    <div class="fetish-col">

      <button class="fetish-btn">Начать мониторинг</button>
      <button class="fetish-btn">Остановить мониторинг</button>
      <button class="fetish-btn">Перезаписать</button>
    
    </div>
  
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { defineComponent } from 'vue';


export default defineComponent ({
  name: "ParamsComponent",
  setup() {
    const aquariumOptions = ref([
      { value: '1', text: 'Осетр (взрослые)' },
      { value: '2', text: 'Осетр (молодь)' },
      { value: '3', text: 'Радужная форель' },
    ]);
    const selectedAquarium = ref(''); // выбранная опция

    const metricOptions = ref([
      { value: 'temperature', text: 'Температура' },
      { value: 'oxygenSaturation', text: 'Насыщение кислородом' },
      { value: 'orp', text: 'Окислительно-восстановительный потенциал' },
      { value: 'salinity', text: 'Соленость' },
      { value: 'waterLevel', text: 'Уровень воды' },
      { value: 'turbidity', text: 'Мутность' },
      { value: 'ammonia', text: 'Аммиак' },
      { value: 'nitrites', text: 'Нитриты' },
      { value: 'ph', text: 'Кислотность' },
    ]);
    const selectedMetric = ref('');

    const fetchOptions = async () => {
      try {
        const response = await axios.get('https://your-api-endpoint.com/aquariumOptions'); // замените на реальный URL API
        aquariumOptions.value = response.data; // сохраняем полученные опции в массив
      } catch (error) {
        console.error(error);
      }
    };

    const handleChange = () => {
      console.log(`Вы выбрали: ${selectedAquarium.value}`);
    };

    return {
      aquariumOptions,
      selectedAquarium,
      metricOptions,
      selectedMetric,
      handleChange,
    };
  },
});
</script>

<style scoped>
.fetish {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: stretch;
  height: 15em;
}

.fetish-col {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  height: 15em;
}

.fetish-btn {
  margin: 10px;
  padding: 2px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.15);
}

.input-label {
  margin: 0 10px;
  padding: 0;
}

.dropdown {
  margin: 10px;
  padding: 0.2em;
  position: relative;
  width: 200px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.15);
}

.dropdown select {
  appearance: none;
  outline: none;
  border: none;
  padding: 12px 20px;
  width: 100%;
  cursor: pointer;
  background-color: white;
  color: #333;
  font-size: 16px;
  box-sizing: border-box;
}

.dropdown::after {
  content: "\25BC";
  position: absolute;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  font-size: 18px;
  color: #777;
  pointer-events: none;
}

/* Состояние фокуса */
.dropdown select:focus {
  border: 2px solid blue;
}

/* Псевдо-класс :hover для эффекта наведения мыши */
.dropdown:hover {
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.35);
}

.input-field {
  margin: 3px 10px;
  border: 1px solid black;
  width: 300px;
  border-radius: 10px;
  padding: 0.2em;
  overflow: hidden;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.15);
}

.input-field input {
  width: 100%;
  padding: 10px 10px;
  border: none;
  background-color: white;
  color: #333;
  box-sizing: border-box;
}

/* Состояние фокуса */
.input-field input:focus {
  border: 2px solid blue;
}

/* Эффект при наведении мыши */
.input-field:hover {
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.35);
}
</style>
