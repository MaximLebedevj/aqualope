<template>
    <div class="mx-auto" style="width: 50vw;">
      <canvas ref="lineChart"></canvas>
      <div class="d-grid gap-2 d-md-flex justify-content-md-center">
        <button type="button" class="btn btn-dark">&lt;</button>
        <button type="button" class="btn btn-info">-</button>
        <button type="button" class="btn btn-info">reset</button>
        <button type="button" class="btn btn-info">+</button>
        <button type="button" class="btn btn-dark">></button>
      </div>
    </div>
  </template>
  
  <script>
  import { onMounted, ref } from "vue";
  import { Chart, registerables } from "chart.js";
  
  Chart.register(...registerables);
  
  export default {
    name: "ChartComponent",
    setup() {
      const lineChart = ref(null);
  
      onMounted(() => {
        const ctx = lineChart.value.getContext("2d");
        new Chart(ctx, {
          type: "line",
          data: {
            labels: Array.from(Array(7).keys()),
            datasets: [{
              label: "data",
              data: [65, 59, 80, 81, 56, 55, 40],
              borderColor: "rgba(75, 192, 192, 1)",
              backgroundColor: "rgba(75, 192, 192, 0.0)",
              fill: true
            }, 
            {
              label: "BottomBorder",
              data: [20, 20, 20, 20, 20, 20, 20],
              borderColor: "rgba(30, 30, 30, 1)",
              backgroundColor: "rgba(30, 30, 30, 0.0)",
              fill: true
            },
            {
              label: "TopBorder",
              data: [90, 90, 90, 90, 90, 90, 90],
              borderColor: "rgba(30, 30, 30, 1)",
              backgroundColor: "rgba(30, 30, 30, 0.0)",
              fill: true
            }]
          },
          options: {
            responsive: true,
            scales: {
              x: {
                title: {
                  display: false,
                  text: "Months"
                }
              },
              y: {
                title: {
                  display: false,
                  text: "Sales Amount"
                }
              }
            }
          }
        });
      });
  
      return { lineChart };
    }
  };
  </script>
  
  <style scoped>
  canvas {
    max-width: 50vw;
    max-height: 50vh;
  }
  .chart {
    max-width: 50vw;
    max-height: 50vh;
  }
  </style>