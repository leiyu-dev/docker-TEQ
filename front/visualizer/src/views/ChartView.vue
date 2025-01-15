<template>
    <span style="font-size: 40px; font-weight: 100; text-align: center;">Overview charts</span>
    <br>
    <el-space wrap :size="25">
      <el-card v-for="(chartOption, index) in this.chartStore.chartOptions" >
        <template #header>
          <div class="card-header">
            <span>{{ chartOption.chartTitle }}</span>
          </div>
        </template>
        <div
        :key="index"
        :ref="'chart'+index"
        class="charts"></div>
      </el-card>
    </el-space>
    <el-divider />
  <el-row :gutter="20">
    <el-col :span="4" style="font-size: 40px; font-weight: 100; text-align: center;">Detail charts</el-col>
    <el-col :span="4">
      <el-select
          v-model="selectedAlgorithm"
          placeholder="Choose Algorithm"
          style="width: 100%"
          size="large"
          @change="fetchLayers"
      >
        <el-option
            v-for="algorithm in algorithms"
            :key="algorithm"
            :label="algorithm"
            :value="algorithm"
        ></el-option>
      </el-select>
    </el-col>
      <el-col :span="4">
        <el-select
            size="large"
            v-model="selectedLayer"
            placeholder="Choose Layer"
            style="width: 100%;"
            @change="fetchNodes"
        >
          <el-option
              v-for="layer in layers"
              :key="layer"
              :label="layer"
              :value="layer"
          ></el-option>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select
            size="large"
            v-model="selectedNode"
            placeholder="Choose Node"
            style="width: 100%;"
        >
          <el-option
              v-for="node in nodes"
              :key="node"
              :label="node"
              :value="node"
          ></el-option>
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" size="large" @click="startInspect">
          <el-icon :size="20" style="color: white; margin-right: 10px;">
            <Plus />
          </el-icon>
          Add Inspect
        </el-button>
      </el-col>
    </el-row>
  <el-space wrap :size="25">
    <el-card v-for="(chartOption, index) in this.chartStore.chartOptionsNode" >
      <template #header>
        <div class="card-header">
          <span>{{ chartOption.chartTitle }}</span>
        </div>
      </template>
      <div
          :key="index"
          :ref="'chart'+(10000+index)"
          class="charts"></div>
    </el-card>
  </el-space>
    <el-divider />
    <span style="font-size: 40px; font-weight: 100; text-align: center;">User defined charts</span>
    <br>
  <el-space wrap :size="25">
    <el-card v-for="(chartOption, index) in this.chartStore.chartOptionsUser" >
      <template #header>
        <div class="card-header">
          <span>{{ chartOption.chartTitle }}</span>
        </div>
      </template>
      <div
          :key="index"
          :ref="'chart'+(20000+index)"
          class="charts"></div>
    </el-card>
  </el-space>
  <el-backtop :right="100" :bottom="100" />


</template>

<script>
import { useChartStore } from '../stores/chart';
import * as echarts from 'echarts';
import axios from "axios";
import {ElMessage} from "element-plus";
// import { ElMessage } from 'element-plus'
export default {
  name: 'ChartView',
  data(){
    return {
      defaultChartList: [],
      nodeChartList: [],
      userChartList: [],
      resizeListenerBound: false,
      intervalId: null,
      chartStore : useChartStore(),
      algorithms: [],
      layers: [],
      nodes: [],
      selectedAlgorithm: '',
      selectedLayer: '',
      selectedNode: '',
    }
  },
  mounted() {
    this.chartStore.chartOptions.forEach((option, index) => {
        const chartRef = this.$refs['chart' + index][0];
        if (chartRef) {
          const chart = echarts.init(chartRef, null, {renderer: 'svg'});
          chart.setOption(option.option);
          this.defaultChartList.push({
            chart: chart,
            chartTitle: option.chartTitle,
          });
        } else {
          console.error(`Chart ref "chart${index}" is undefined`);
        }
    });
    this.chartStore.chartOptionsNode.forEach((option, index) => {
      const chartRef = this.$refs['chart' + (index + 10000)][0];
      if (chartRef) {
        const chart = echarts.init(chartRef, null, {renderer: 'svg'});
        chart.setOption(option.option);
        this.nodeChartList.push({
          chart: chart,
          chartTitle: option.chartTitle,
        });
      } else {
        console.error(`Chart ref "node chart${index}" is undefined`);
      }
    });
    this.chartStore.chartOptionsUser.forEach((option, index) => {
      const chartRef = this.$refs['chart' + (index + 20000)][0];
      if (chartRef) {
        const chart = echarts.init(chartRef, null, {renderer: 'svg'});
        chart.setOption(option.option);
        this.userChartList.push({
          chart: chart,
          chartTitle: option.chartTitle,
        });
      } else {
        console.error(`Chart ref "user chart${index}" is undefined`);
      }
    });
    this.intervalId = setInterval(() => {
      for(let i=0; i<this.defaultChartList.length; i++){
        let chart = this.defaultChartList[i].chart;
        let chartTitle = this.defaultChartList[i].chartTitle;
        let index = this.chartStore.chartMap.get(chartTitle);
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[index].length; j++){
          seriesList.push({
            data: this.chartStore.yData[index][j]
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index]
          }
        });
      }
      for(let i=0; i<this.nodeChartList.length; i++){
        let chart = this.nodeChartList[i].chart;
        let chartTitle = this.nodeChartList[i].chartTitle;
        let index = this.chartStore.chartMap.get(chartTitle);
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[index].length; j++){
          seriesList.push({
            data: this.chartStore.yData[index][j]
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index]
          }
        });
      }
      for(let i=0; i<this.userChartList.length; i++){
        let chart = this.userChartList[i].chart;
        let chartTitle = this.userChartList[i].chartTitle;
        let index = this.chartStore.chartMap.get(chartTitle);
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[index].length; j++){
          seriesList.push({
            data: this.chartStore.yData[index][j]
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index]
          }
        });
      }
    }, 1000);
    this.fetchAlgorithms();
  },
  methods:{
    async fetchAlgorithms() {
      try {
        const response = await axios.get("http://localhost:8889/algorithm");
        this.algorithms = response.data;
      } catch (error) {
        ElMessage.error("Error fetching algorithms:", error);
      }
    },
    async fetchLayers() {
      try {
        const response = await axios.get("http://localhost:8889/layer", {
          params: {
            ElMessage: this.selectedAlgorithm,
          },
        });
        this.layers = response.data;
      } catch (error) {
        ElMessage.error("Error fetching layers:", error);
      }
    },
    async fetchNodes() {
      // this.configNodes.length = 0;
      if (!this.selectedAlgorithm || !this.selectedLayer) {
        return;
      }
      try {
        const response = await axios.get("http://localhost:8889/node", {
          params: {
            algorithm: this.selectedAlgorithm,
            layer: this.selectedLayer,
          },
        });
        // response.data.push("All nodes")
        this.nodes = response.data;
      } catch (error) {
        ElMessage.error("Error fetching nodes:", error);
      }
    },
     async startInspect() {
       // 检查是否已经添加过
       for (let i = 0; i < this.nodeChartList.length; i++) {
         if (this.nodeChartList[i].chartTitle.includes(this.selectedNode)) {
           ElMessage.error("This node has been added");
           return;
         }
       }

       if (!this.selectedAlgorithm || !this.selectedLayer || !this.selectedNode) {
         return;
       }
       try {
         const response = await axios.post("http://localhost:8889/inspect", {
           algorithm: this.selectedAlgorithm,
           layer: this.selectedLayer,
           node: this.selectedNode,
         });
         const chartList = await response.data;
         for (const rawChart of chartList) {
           console.log("add chart", rawChart);
           this.chartStore.addChart(rawChart);
           await this.$nextTick(() => {
             let index = this.chartStore.chartOptionsNode.length - 1;
             let option = this.chartStore.chartOptionsNode[index];
             const chartRef = this.$refs['chart' + (index + 10000)][0];
             if (chartRef) {
               const chart = echarts.init(chartRef, null, {renderer: 'svg'});
               chart.setOption(option.option);
               this.nodeChartList.push({
                 chart: chart,
                 chartTitle: option.chartTitle,
               });
             } else {
               console.error(`Chart ref "node chart${index}" is undefined`);
             }
           });
         }
         ElMessage.success("Inspect started successfully");
       } catch (error) {
         ElMessage.error("Error starting inspect:", error);
       }
     }
  }
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