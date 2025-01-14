<template>
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
  <el-backtop :right="100" :bottom="100" />


</template>

<script>
import { useChartStore } from '../stores/chart';
import * as echarts from 'echarts';
// import { ElMessage } from 'element-plus'
export default {
  name: 'ChartView',
  data(){
    return {
      chartList: [],
      intervalId: null,
      chartStore : useChartStore()
    }
  },
  mounted() {
    this.chartStore.chartOptions.forEach((option, index) => {
      const chartRef = this.$refs['chart' + index][0];
      if (chartRef) {
        const chart = echarts.init(chartRef, null, { renderer: 'svg' });
        chart.setOption(option);
        this.chartList.push(chart);
      } else {
        console.error(`Chart ref "chart${index}" is undefined`);
      }
    });
    if (!this.resizeListenerBound) {
      window.addEventListener('resize', () => {
        this.chartList.forEach(chart => chart.resize());
      });
      this.resizeListenerBound = true;
    }
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
.el-card {
  text-align: center;
}
</style>