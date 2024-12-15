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
  </div>
</template>

<script>
import * as echarts from 'echarts';
export default {
  name: 'EChartsExample',
  data(){
    return {
      chartOptions : [],
      xData : [],
      yData : [],
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
        let seriesList = []
        this.yData.push([]);
        this.xData.push([]);
        for(let i=0;i<rawChart.dataLabel.length;i++){
          this.yData[this.chartCount].push([]);
          let dataName = rawChart.dataLabel[i];
          seriesList.push({
            type: 'line',
            data: this.yData[this.chartCount][i],
            stack: 'Total',
            name: dataName,
            smooth: true,
          })
        }
        let options = {
          xAxis: {
            type: 'category',
            boundaryGap: false,
            name: rawChart.xLabel,
            data: this.xData[this.chartCount]
          },
          tooltip: {
            trigger: 'axis'
          },
          toolbox: {
            feature: {
              saveAsImage: {}
            }
          },

          legend : {
            data: rawChart.dataLabel
          },
          yAxis: {
            type: 'value',
            name: rawChart.yLabel
          },
          series:seriesList
        }

        // console.log(options)
        this.chartOptions.push(options);
        this.chartMap.set(rawChart.title, this.chartCount);
        this.chartTitle.push(rawChart.title);
        this.chartCount++;

      }
      this.$nextTick(() => {
        this.chartOptions.forEach((option, index) => {
          const chartRef = this.$refs['chart' + index][0];
          // console.log(chartRef)
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
    this.intervalId = setInterval(() => {
      this.fetchData();
    }, 1000);
  },
  methods: {
    async fetchData(){
      fetch('http://localhost:8889/data').then(v => v.json()).then((response) => {
        //response: [{chartName: "..",xData: "1", yData: ["1","2","3","4","5"]}, ...]
        for(let rawData of response){
          let chartIndex = this.chartMap.get(rawData.chartName);
          this.xData[chartIndex].push(rawData.xData)
          for(let i=0;i<rawData.yData.length;i++) {
            let yData=rawData.yData[i];
            this.yData[chartIndex][i].push(yData);
          }
        }
        for(let i=0; i<this.chartList.length; i++){
          let chart = this.chartList[i];
          let seriesList = [];
          for(let j=0; j<this.yData[i].length; j++){
            seriesList.push({
              data: this.yData[i][j]
            });
          }
          chart.setOption({
            series:seriesList,
            xAxis: {
              data: this.xData[i]
            }
          });
        }
      })
    },
  }
};
</script>

<style>
.charts {
  width: 600px;
  height: 300px;
}
</style>