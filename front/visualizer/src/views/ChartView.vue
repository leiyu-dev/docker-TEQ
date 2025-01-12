<template>
  <main>
    <span style="font-size: 40px; font-weight: 100; text-align: center;">Overview charts</span>
    <br>
    <el-space wrap :size="25">
      <el-card v-for="(chartOption, index) in this.chartStore.chartOptions" >
        <template #header>
          <div class="card-header">
            <span>{{ this.chartStore.chartTitle[index] }}</span>
          </div>
        </template>
        <div
        :key="index"
        :ref="'chart'+index"
        class="charts"></div>
      </el-card>
    </el-space>
    <el-divider />
    <span style="font-size: 40px; font-weight: 100; text-align: center;">Detail charts</span>
    <br>

    <el-divider />
    <span style="font-size: 40px; font-weight: 100; text-align: center;">User defined charts</span>
    <br>


  </main>
</template>

<script>
import { useChartStore } from '../stores/chart';
import * as echarts from 'echarts';

export default {
  name: 'ChartView',
  data(){
    return {
      chartList: [],
      intervalId: null,
    }
  },
  setup() {
    const chartStore = useChartStore();
    return { chartStore };
  },
  mounted() {
    //wait until the chartOptions are set in the store
    let count = 0;
    while( this.chartStore.chartOptions.length === 0 && count < 10){
      count++;
      new Promise(r => setTimeout(r, 200));
    }
    this.chartStore.chartOptions.forEach((option, index) => {
      const chartRef = this.$refs['chart' + index][0];
      if (chartRef) {
        const chart = echarts.init(chartRef);
        chart.setOption(option);
        this.chartList.push(chart);

        window.addEventListener('resize', () => {
          chart.resize();
        });
      } else {
        console.error(`Chart ref "chart${index}" is undefined`);
      }
    });
    this.intervalId = setInterval(() => {
      for(let i=0; i<this.chartList.length; i++){
        let chart = this.chartList[i];
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[i].length; j++){
          seriesList.push({
            data: this.chartStore.yData[i][j]
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[i]
          }
        });
      }
    }, 1000);
  },
};
</script>




<style>
.charts {
  width: 500px;
  height: 300px;
}
</style>