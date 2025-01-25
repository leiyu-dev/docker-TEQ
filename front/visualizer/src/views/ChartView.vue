<template>
  <!-- Overview charts -->
  <el-row type="flex" align="middle" justify="start" style="margin-bottom: 20px;">
    <el-col :span="2" style="text-align: left;">
      <el-button size="large" @click="toggleSection('overview')">
        {{ showOverview ? 'Collapse' : 'Expand' }}
      </el-button>
    </el-col>
    <el-col :span="8" style="text-align: left;">
      <span style="font-size: 40px; font-weight: 100;">Overview charts</span>
    </el-col>
  </el-row>
  <el-space v-if="showOverview" wrap :size="25" style="margin-top: 10px;">
    <el-card v-for="(chartOption, index) in this.chartStore.chartOptions" :key="index">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ chartOption.chartTitle }}</span>
          <el-button type="text" @click="showFullChart(index, 'overview')">View Full Chart</el-button>
        </div>
      </template>
      <div :ref="'chart' + index" class="charts"></div>
    </el-card>
  </el-space>

  <el-divider style="margin: 20px 0;" />

  <!-- Detail charts -->
  <el-row type="flex" align="middle" justify="start" style="margin-bottom: 20px;">
    <el-col :span="2" style="text-align: left;">
      <el-button size="large" @click="toggleSection('detail')">
        {{ showDetail ? 'Collapse' : 'Expand' }}
      </el-button>
    </el-col>
    <el-col :span="4" style="text-align: left;">
      <span style="font-size: 40px; font-weight: 100;">Detail charts</span>
    </el-col>
  </el-row>
  <el-row :gutter="20" v-if="showDetail" style="margin-bottom: 20px;">
<!--    <el-col :span="4">-->
<!--      <el-select-->
<!--          v-model="selectedAlgorithm"-->
<!--          placeholder="Choose Algorithm"-->
<!--          style="width: 100%"-->
<!--          size="large"-->
<!--          @change="fetchLayers"-->
<!--      >-->
<!--        <el-option-->
<!--            v-for="algorithm in algorithms"-->
<!--            :key="algorithm"-->
<!--            :label="algorithm"-->
<!--            :value="algorithm"-->
<!--        ></el-option>-->
<!--      </el-select>-->
<!--    </el-col>-->
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
  <el-space v-if="showDetail" wrap :size="25" style="margin-top: 10px;">
    <el-card v-for="(chartOption, index) in this.chartStore.chartOptionsNode" :key="'detail-' + index">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ chartOption.chartTitle }}</span>
          <el-button type="text" @click="showFullChart(index, 'detail')">View Full Chart</el-button>
        </div>
      </template>
      <div :ref="'chart' + (index + 10000)" class="charts"></div>
    </el-card>
  </el-space>

  <el-divider style="margin: 20px 0;" />

  <!-- User defined charts -->
  <el-row type="flex" align="middle" justify="start" style="margin-bottom: 20px;">
    <el-col :span="2" style="text-align: left;">
      <el-button size="large" @click="toggleSection('user')">
        {{ showUser ? 'Collapse' : 'Expand' }}
      </el-button>
    </el-col>
    <el-col :span="8" style="text-align: left;">
      <span style="font-size: 40px; font-weight: 100;">User defined charts</span>
    </el-col>
  </el-row>
  <el-space v-if="showUser" wrap :size="25">
    <el-card v-for="(chartOption, index) in this.chartStore.chartOptionsUser" :key="'user-' + index">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ chartOption.chartTitle }}</span>
          <el-button type="text" @click="showFullChart(index, 'user')">View Full Chart</el-button>
        </div>
      </template>
      <div :ref="'chart' + (index + 20000)" class="charts"></div>
    </el-card>
  </el-space>


  <el-dialog v-model="dialogVisible" style="width:80%" destroy-on-close :close-on-click-modal="false" :before-close="closeDialog">
    <template #title>
      <div style="display: flex; justify-content: center; align-items: center; text-align: center;">
        <span style="font-size: 20px; font-weight: 400;">{{ dialogChartTitle }}</span>
      </div>
    </template>
    <div ref="dialogChart" style="width: 100%; height: 500px;"></div>
  </el-dialog>

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
      intervalId: null,
      chartStore : useChartStore(),
      algorithms: [],
      layers: [],
      nodes: [],
      selectedAlgorithm: '',
      selectedLayer: '',
      selectedNode: '',
      showOverview: true,
      showDetail: true,
      showUser: true,
      dialogVisible: false, // Controls dialog visibility
      dialogChartTitle: '', // Title of the chart in dialog
      dialogChartInstance: null, // Chart instance for dialog
    }
  },
  mounted() {
    setTimeout( () => {
      this.initOverview()
      this.initNode()
      this.initUser()
    },300);
    this.intervalId = setInterval(() => {
      for(let i=0; i<this.defaultChartList.length; i++){
        let chart = this.defaultChartList[i].chart;
        let chartTitle = this.defaultChartList[i].chartTitle;
        let index = this.chartStore.chartMap.get(chartTitle);
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[index].length; j++){
          seriesList.push({
            data: this.chartStore.yData[index][j].slice(-20)
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index].slice(-20)
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
            data: this.chartStore.yData[index][j].slice(-20)
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index].slice(-20)
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
            data: this.chartStore.yData[index][j].slice(-20)
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index].slice(-20)
          }
        });
      }
    }, 3000);
    this.fetchLayers();
  },
  methods:{
    initOverview(){
      this.chartStore.chartOptions.forEach((option, index) => {
        // if the chart has already been initialized, destroy it first
        if (this.defaultChartList[index]) {
          this.defaultChartList[index].chart.dispose();
        }
      });
      this.defaultChartList = [];
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
    },
    initNode(){
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
    },
    initUser(){
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
    },
    toggleSection(section) {
      if (section === 'overview') {
        this.showOverview = !this.showOverview;
        if(this.showOverview === true){
          this.$nextTick(() => {
            this.initOverview();
          });
        }
      } else if (section === 'detail') {
        this.showDetail = !this.showDetail;
        if(this.showDetail === true){
          this.$nextTick(() => {
            this.initNode();
          });
        }
      } else if (section === 'user') {
        this.showUser = !this.showUser;
        if(this.showUser === true){
          this.$nextTick(() => {
            this.initUser();
          });
        }
      }
    },
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
            algorithm: "Algorithm1",
          },
        });
        this.layers = response.data;
      } catch (error) {
        ElMessage.error("Error fetching layers:", error);
      }
    },
    async fetchNodes() {
      // this.configNodes.length = 0;
      if (!"Algorithm1" || !this.selectedLayer) {
        return;
      }
      try {
        const response = await axios.get("http://localhost:8889/node", {
          params: {
            algorithm: "Algorithm1",
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

       if (!"Algorithm1" || !this.selectedLayer || !this.selectedNode) {
         return;
       }
       try {
         const response = await axios.post("http://localhost:8889/inspect", {
           algorithm: "Algorithm1",
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
     },
    showFullChart(i, type) {
      // Determine the chart list based on type
      const chartList =
          type === 'overview' ? this.defaultChartList :
              type === 'detail' ? this.nodeChartList :
                  this.userChartList;
      let originChart = chartList[i].chart;
      let chartTitle = chartList[i].chartTitle;
      let index = this.chartStore.chartMap.get(chartTitle);
      let seriesList = [];
      for(let j=0; j<this.chartStore.yData[index].length; j++){
        seriesList.push({
          sampling: 'lttb',
          type: 'line',
          connectNulls: true,
          name: originChart.getOption().series[j].name,
          data: this.chartStore.yData[index][j],
          symbol: 'none',
        });
        console.log("name:",originChart.getOption().series[j].name);
      }
      this.dialogChartTitle = chartTitle;
      this.dialogVisible = true;
      this.$nextTick(() => {
        const chartRef = this.$refs.dialogChart;
        if (this.dialogChartInstance) {
          this.dialogChartInstance.dispose();
        }
        const chart = echarts.init(chartRef, null, { renderer: 'svg' });
        chart.setOption({
          dataZoom: [
              {
                  type: "slider",
                  start: 0,
                  end: 100,
              },
              {
                type: 'inside',
                start: 0,
                end: 100
              }
          ],
          tooltip: {
            trigger: 'axis',
            position: function (pt) {
              return [pt[0], '10%'];
            }
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            name: originChart.getOption().xAxis[0].name,
            data: this.chartStore.xData[index],
          },
          toolbox: {
            feature: {
              saveAsImage: {}
            },
          },
          legend: {
            data: originChart.getOption().legend[0].data,
          },
          yAxis: {
            type: 'value',
            nameLocation: 'center',
            nameGap: 45,
            name: originChart.getOption().yAxis[0].name,
          },
          series: seriesList,
        });
        this.dialogChartInstance = chart;
      });

      },
      closeDialog() {
        this.dialogVisible = false;
        if (this.dialogChartInstance) {
          this.dialogChartInstance.dispose();
          this.dialogChartInstance = null;
        }
      },
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