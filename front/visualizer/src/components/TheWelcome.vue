<template>
  <div>
    <div v-for="(chartOption, index) in chartOptions"
    :key="index"
    :ref="'chart'+index"></div>
    <el-button @click="updateData">更新数据</el-button>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import axios from 'axios';
export default {
  name: 'EChartsExample',
  data(){
    return {
      chartOptions : [],
      chartData : [],
      chartCount: 0
    }
  },
  mounted() {
    fetch('http://localhost:8889/chart').then(v=>v.json()).then((response) => {
      const chartList = response;
      for(let rawChart of chartList){
        this.chartData.push([]);
        let options = {
          title: {
            text: rawChart.title
          },
          xAxis: {
            name: rawChart.xLabel
          },
          yAxis: {
            name: rawChart.yLabel
          },
          series: [
            {
              type: 'line',
              data: this.chartData[this.chartCount]
            }
          ]
        }
        this.chartOptions.push(options);
        this.chartCount++;
        const chart = echarts.init(this.$refs.chart + this.chartCount);
        chart.setOption(options);
        window.addEventListener('resize', () => {
          chart.resize();
        });
      }
    });
  },
  methods: {
    updateData() {
      const socket = new WebSocket('ws://localhost:');
      const chart = echarts.init(this.$refs.chart);
      const options = {
        title: {
          text: 'ECharts 示例',
        },
        tooltip: {},
        xAxis: {
          type: 'category',
          data: ['苹果', '香蕉', '橙子', '葡萄'],
        },
        yAxis: {
          type: 'value',
        },
        series: [
          {
            name: '销量',
            type: 'bar',
            data: [10, 30, 50, 20],
          },
        ],
      };
      chart.setOption(options);
    },
  }
};
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
}
</style>
