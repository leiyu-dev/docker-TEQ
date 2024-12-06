<template>
  <div>
    <el-space wrap :size="30">
      <el-card v-for="(chartOption, index) in chartOptions" >
        <template #header>
          <div class="card-header">
            <span>{{ chartTitle[index] }}</span>
          </div>
        </template>
        <div
        :key="index"
        :ref="'chart'+index"
        class="charts"></div>
      </el-card>
    </el-space>
    <el-button @click="updateData">更新数据</el-button>
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default {
  name: 'EChartsExample',
  data(){
    return {
      chartOptions : [],
      chartData : [],
      chartCount: 0,
      chartMap: null,
      chartList: [],
      intervalId: null,
      chartTitle: []
    }
  },
  mounted() {
    this.chartMap = new Map()
    fetch('http://localhost:8889/chart').then(v=>v.json()).then((response) => {
      const chartList = response;
      for(let rawChart of chartList){
        this.chartData.push([]);
        let options = {
          xAxis: {
            type: 'category',
            name: rawChart.xLabel
          },
          yAxis: {
            name: rawChart.yLabel
          },
          series: [
            {
              type: 'line',
              data: this.chartData[this.chartCount],
              // symbol: 'none'
            }
          ]
        }

        console.log(options)
        this.chartOptions.push(options);
        this.chartMap.set(rawChart.title, this.chartCount);
        this.chartTitle.push(rawChart.title);
        this.chartCount++;

      }
      this.$nextTick(() => {
        this.chartOptions.forEach((option, index) => {
          const chartRef = this.$refs['chart' + index][0];
          console.log(chartRef)
          if (chartRef) {
            const chart = echarts.init(chartRef);
            chart.setOption(option);
            this.chartList.push(chart)

            // 添加 resize 事件
            window.addEventListener('resize', () => {
              chart.resize();
            });
          } else {
            console.error(`Chart ref "chart${index}" is undefined`);
          }
        });
      });
    });
  },
  methods: {
    async fetchData(){
      fetch('http://localhost:8889/data').then(v => v.json()).then((response) => {
        //response: [{chartName: "..",xData: "1", yData: "2"}, ...]
        for(let rawData of response){
          let chartIndex = this.chartMap.get(rawData.chartName);
          this.chartData[chartIndex].push([rawData.xData, rawData.yData]);
        }
        for(let i=0; i<this.chartList.length; i++){
          let chart = this.chartList[i];
          chart.setOption({
            series: [
              {
                data: this.chartData[i]
              }
            ]
          });
        }
      })
    },
    updateData() {
      this.intervalId = setInterval(() => {
        this.fetchData();
      }, 1000);
    }
  }
};
</script>

<style>
.charts {
  width: 600px;
  height: 300px;
}
</style>
