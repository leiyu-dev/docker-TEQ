<template>
  <div class="charts-container fade-in">
    <!-- 页面标题和描述 -->
    <div class="page-header slide-in-up">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title gradient-text">数据可视化中心</h1>
          <p class="page-subtitle">实时监控系统运行状态，支持多维度数据分析和自定义图表配置</p>
        </div>
      </div>
    </div>

    <!-- Overview charts -->
    <div class="chart-section slide-in-up" style="animation-delay: 0.1s;">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title gradient-text">总览图表</h2>
          <p class="section-subtitle">系统整体运行状态监控</p>
        </div>
        <div class="header-actions">
          <el-button 
            size="large" 
            @click="toggleSection('overview')"
            :type="showOverview ? 'primary' : 'default'"
            class="toggle-btn glow-on-hover"
          >
            <el-icon v-if="showOverview"><Minus /></el-icon>
            <el-icon v-else><Plus /></el-icon>
            {{ showOverview ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>
      
      <div v-if="showOverview" class="charts-grid">
        <el-card 
          v-for="(chartOption, index) in this.chartStore.chartOptions" 
          :key="index"
          class="chart-card hover-lift"
        >
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="card-title">{{ chartOption.chartTitle }}</span>
                <el-tag type="success" size="small">实时监控</el-tag>
              </div>
              <el-button 
                type="primary" 
                size="small" 
                @click="showFullChart(index, 'overview')"
                class="view-full-btn"
              >
                <el-icon><FullScreen /></el-icon>
                查看详情
              </el-button>
            </div>
          </template>
          <div :ref="'chart' + index" class="chart-container"></div>
        </el-card>
      </div>

      <div v-else class="collapsed-info">
        <el-empty 
          description="总览图表已收起"
          :image-size="60"
        >
          <template #image>
            <div class="collapsed-icon">
              <el-icon :size="40"><TrendCharts /></el-icon>
            </div>
          </template>
        </el-empty>
      </div>
    </div>

    <!-- Detail charts -->
    <div class="chart-section slide-in-up" style="animation-delay: 0.2s;">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title gradient-text">详细图表</h2>
          <p class="section-subtitle">节点级别的详细监控数据</p>
        </div>
        <div class="header-actions">
          <el-button 
            size="large" 
            @click="toggleSection('detail')"
            :type="showDetail ? 'primary' : 'default'"
            class="toggle-btn glow-on-hover"
          >
            <el-icon v-if="showDetail"><Minus /></el-icon>
            <el-icon v-else><Plus /></el-icon>
            {{ showDetail ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>

      <div v-if="showDetail" class="detail-content">
        <el-card class="selector-card hover-lift glass-effect">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <span class="card-title">监控配置</span>
                <el-tag type="info" size="small">实时数据</el-tag>
              </div>
              <div class="header-right">
                <div class="status-indicators">
                  <div class="status-item">
                    <span class="status-label">当前层级：</span>
                    <el-tag v-if="selectedLayer" type="primary" size="small">
                      {{ selectedLayer }}
                    </el-tag>
                    <span v-else class="status-empty">未选择</span>
                  </div>
                  <div class="status-item">
                    <span class="status-label">当前节点：</span>
                    <el-tag v-if="selectedNode" type="success" size="small">
                      {{ selectedNode }}
                    </el-tag>
                    <span v-else class="status-empty">未选择</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <div class="selector-row">
            <div class="selector-item">
              <label class="selector-label">
                <el-icon><Tickets /></el-icon>
                选择层级
              </label>
              <el-select
                size="large"
                v-model="selectedLayer"
                placeholder="选择层级"
                @change="fetchNodes"
                class="modern-select"
              >
                <el-option
                  v-for="layer in layers"
                  :key="layer"
                  :label="layer"
                  :value="layer"
                ></el-option>
              </el-select>
            </div>
            
            <div class="selector-item">
              <label class="selector-label">
                <el-icon><Monitor /></el-icon>
                选择节点
              </label>
              <el-select
                size="large"
                v-model="selectedNode"
                placeholder="选择节点"
                class="modern-select"
                :disabled="!selectedLayer"
              >
                <el-option
                  v-for="node in nodes"
                  :key="node"
                  :label="node"
                  :value="node"
                ></el-option>
              </el-select>
            </div>
            
            <div class="selector-item">
              <el-button 
                type="primary" 
                size="large" 
                @click="startInspect"
                class="add-inspect-btn glow-on-hover"
                :disabled="!selectedLayer || !selectedNode"
              >
                <el-icon><Plus /></el-icon>
                添加监控
              </el-button>
            </div>
          </div>
        </el-card>

        <div class="charts-grid" v-if="chartStore.chartOptionsNode.length > 0">
          <el-card 
            v-for="(chartOption, index) in this.chartStore.chartOptionsNode" 
            :key="'detail-' + index"
            class="chart-card hover-lift"
          >
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <span class="card-title">{{ chartOption.chartTitle }}</span>
                  <el-tag type="warning" size="small" style="margin-right: 10px;">节点监控</el-tag>
                </div>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="showFullChart(index, 'detail')"
                  class="view-full-btn"
                >
                  <el-icon><FullScreen /></el-icon>
                  查看详情
                </el-button>
              </div>
            </template>
            <div :ref="'chart' + (index + 10000)" class="chart-container"></div>
          </el-card>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">
            <el-icon size="64"><DocumentAdd /></el-icon>
          </div>
          <h3>暂无详细图表</h3>
          <p>选择层级和节点后点击"添加监控"开始监控数据</p>
          <el-button type="primary" @click="scrollToSelector" class="quick-setup-btn">
            <el-icon><Setting /></el-icon>
            快速配置
          </el-button>
        </div>
      </div>

      <div v-else class="collapsed-info">
        <el-empty 
          description="详细图表已收起"
          :image-size="60"
        >
          <template #image>
            <div class="collapsed-icon">
              <el-icon :size="40"><Monitor /></el-icon>
            </div>
          </template>
        </el-empty>
      </div>
    </div>

    <!-- User defined charts -->
    <div class="chart-section slide-in-up" style="animation-delay: 0.3s;">
      <div class="section-header">
        <div class="header-left">
          <h2 class="section-title gradient-text">自定义图表</h2>
          <p class="section-subtitle">用户自定义的监控图表</p>
        </div>
        <div class="header-actions">
          <el-button 
            size="large" 
            @click="toggleSection('user')"
            :type="showUser ? 'primary' : 'default'"
            class="toggle-btn glow-on-hover"
          >
            <el-icon v-if="showUser"><Minus /></el-icon>
            <el-icon v-else><Plus /></el-icon>
            {{ showUser ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>

      <div v-if="showUser" class="user-content">
        <div class="charts-grid" v-if="chartStore.chartOptionsUser.length > 0">
          <el-card 
            v-for="(chartOption, index) in this.chartStore.chartOptionsUser" 
            :key="'user-' + index"
            class="chart-card hover-lift"
          >
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <span class="card-title">{{ chartOption.chartTitle }}</span>
                  <el-tag type="info" size="small">自定义</el-tag>
                </div>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="showFullChart(index, 'user')"
                  class="view-full-btn"
                >
                  <el-icon><FullScreen /></el-icon>
                  查看详情
                </el-button>
              </div>
            </template>
            <div :ref="'chart' + (index + 20000)" class="chart-container"></div>
          </el-card>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">
            <el-icon size="64"><DocumentAdd /></el-icon>
          </div>
          <h3>暂无自定义图表</h3>
          <p>您还没有创建任何自定义图表，快来创建第一个吧！</p>
          <el-button type="success" class="create-chart-btn glow-on-hover">
            <el-icon><Plus /></el-icon>
            创建图表
          </el-button>
        </div>
      </div>

      <div v-else class="collapsed-info">
        <el-empty 
          description="自定义图表已收起"
          :image-size="60"
        >
          <template #image>
            <div class="collapsed-icon">
              <el-icon :size="40"><PieChart /></el-icon>
            </div>
          </template>
        </el-empty>
      </div>
    </div>

    <!-- Chart Dialog -->
    <el-dialog 
      v-model="dialogVisible" 
      width="80%"
      :title="dialogChartTitle"
      destroy-on-close 
      :close-on-click-modal="false" 
      :before-close="closeDialog"
      class="chart-dialog"
      center
      :modal-class="'chart-dialog-modal'"
    >
      <div ref="dialogChart" class="dialog-chart-container"></div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDialog" size="large">关闭</el-button>
          <el-button type="primary" size="large" class="export-btn">
            <el-icon><Download /></el-icon>
            导出图表
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-backtop :right="40" :bottom="40" />
  </div>
</template>

<script>
import { useChartStore } from '../stores/chart';
import * as echarts from 'echarts';
import axios from "axios";
import { ElMessage } from "element-plus";
import { 
  Plus, 
  Minus, 
  FullScreen, 
  DocumentAdd, 
  Tickets, 
  Monitor,
  Setting,
  TrendCharts,
  PieChart,
  Download
} from '@element-plus/icons-vue';

export default {
  name: 'ChartView',
  components: {
    Plus,
    Minus,
    FullScreen,
    DocumentAdd,
    Tickets,
    Monitor,
    Setting,
    TrendCharts,
    PieChart,
    Download
  },
  data(){
    return {
      defaultChartList: [],
      nodeChartList: [],
      userChartList: [],
      intervalId: null,
      resizeListener: null,
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
      dialogVisible: false,
      dialogChartTitle: '',
      dialogChartInstance: null,
    }
  },
  mounted() {
    setTimeout( () => {
      this.initOverview()
      this.initNode()
      this.initUser()
    },300);
    
    // 添加窗口大小变化监听器
    this.resizeListener = () => {
      setTimeout(() => {
        this.resizeAllCharts();
      }, 100);
    };
    window.addEventListener('resize', this.resizeListener);
    
    this.intervalId = setInterval(() => {
      // 更新总览图表
      for(let i=0; i<this.defaultChartList.length; i++){
        let chart = this.defaultChartList[i].chart;
        let chartTitle = this.defaultChartList[i].chartTitle;
        let index = this.chartStore.chartMap.get(chartTitle);
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[index].length; j++){
          seriesList.push({
            data: this.chartStore.yData[index][j].slice(-20),
            // 保持原有的样式增强
            animationDuration: 500,
            animationEasing: 'cubicOut'
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index].slice(-20)
          }
        }, { notMerge: false });
      }
      
      // 更新节点图表
      for(let i=0; i<this.nodeChartList.length; i++){
        let chart = this.nodeChartList[i].chart;
        let chartTitle = this.nodeChartList[i].chartTitle;
        let index = this.chartStore.chartMap.get(chartTitle);
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[index].length; j++){
          seriesList.push({
            data: this.chartStore.yData[index][j].slice(-20),
            // 保持原有的样式增强
            animationDuration: 500,
            animationEasing: 'cubicOut'
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index].slice(-20)
          }
        }, { notMerge: false });
      }
      
      // 更新用户图表
      for(let i=0; i<this.userChartList.length; i++){
        let chart = this.userChartList[i].chart;
        let chartTitle = this.userChartList[i].chartTitle;
        let index = this.chartStore.chartMap.get(chartTitle);
        let seriesList = [];
        for(let j=0; j<this.chartStore.yData[index].length; j++){
          seriesList.push({
            data: this.chartStore.yData[index][j].slice(-20),
            // 保持原有的样式增强
            animationDuration: 500,
            animationEasing: 'cubicOut'
          });
        }
        chart.setOption({
          series:seriesList,
          xAxis: {
            data: this.chartStore.xData[index].slice(-20)
          }
        }, { notMerge: false });
      }
    }, 1000);
    this.fetchLayers();
  },
  beforeUnmount() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    
    // 清除窗口大小变化监听器
    if (this.resizeListener) {
      window.removeEventListener('resize', this.resizeListener);
    }
    
    // 销毁所有图表实例
    this.defaultChartList.forEach(chartItem => {
      if (chartItem.chart && !chartItem.chart.isDisposed()) {
        chartItem.chart.dispose();
      }
    });
    this.nodeChartList.forEach(chartItem => {
      if (chartItem.chart && !chartItem.chart.isDisposed()) {
        chartItem.chart.dispose();
      }
    });
    this.userChartList.forEach(chartItem => {
      if (chartItem.chart && !chartItem.chart.isDisposed()) {
        chartItem.chart.dispose();
      }
    });
    
    if (this.dialogChartInstance) {
      this.dialogChartInstance.dispose();
    }
  },
  methods:{
    initOverview(){
      this.chartStore.chartOptions.forEach((option, index) => {
        if (this.defaultChartList[index]) {
          this.defaultChartList[index].chart.dispose();
        }
      });
      this.defaultChartList = [];
      this.chartStore.chartOptions.forEach((option, index) => {
        const chartRef = this.$refs['chart' + index];
        if (chartRef && chartRef[0]) {
          const chartContainer = chartRef[0];
          const chart = echarts.init(chartContainer, null, {renderer: 'svg'});
          
          // 增强图表样式配置
          const enhancedOption = this.enhanceChartOption(option.option);
          chart.setOption(enhancedOption);
          
          // 确保图表正确调整大小
          setTimeout(() => {
            chart.resize();
          }, 100);
          
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
      // 清理现有的节点图表
      this.nodeChartList.forEach(chartItem => {
        if (chartItem.chart && !chartItem.chart.isDisposed()) {
          chartItem.chart.dispose();
        }
      });
      this.nodeChartList = [];
      
      this.chartStore.chartOptionsNode.forEach((option, index) => {
        const chartRef = this.$refs['chart' + (index + 10000)];
        if (chartRef && chartRef[0]) {
          const chartContainer = chartRef[0];
          const chart = echarts.init(chartContainer, null, {renderer: 'svg'});
          
          // 增强图表样式配置
          const enhancedOption = this.enhanceChartOption(option.option);
          chart.setOption(enhancedOption);
          
          // 确保图表正确调整大小
          setTimeout(() => {
            chart.resize();
          }, 100);
          
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
        const chartRef = this.$refs['chart' + (index + 20000)];
        if (chartRef && chartRef[0]) {
          const chartContainer = chartRef[0];
          const chart = echarts.init(chartContainer, null, {renderer: 'svg'});
          
          // 增强图表样式配置
          const enhancedOption = this.enhanceChartOption(option.option);
          chart.setOption(enhancedOption);
          
          // 确保图表正确调整大小
          setTimeout(() => {
            chart.resize();
          }, 100);
          
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
    scrollToSelector() {
      const selectorCard = document.querySelector('.selector-card');
      if (selectorCard) {
        selectorCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    },
    async fetchAlgorithms() {
      try {
        const response = await axios.get("http://localhost:8889/algorithm");
        this.algorithms = response.data;
      } catch (error) {
        ElMessage.error("获取算法列表失败: " + error);
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
        ElMessage.error("获取层级列表失败: " + error);
      }
    },
    async fetchNodes() {
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
        this.nodes = response.data;
      } catch (error) {
        ElMessage.error("获取节点列表失败: " + error);
      }
    },
     async startInspect() {
       for (let i = 0; i < this.nodeChartList.length; i++) {
         if (this.nodeChartList[i].chartTitle.includes(this.selectedNode)) {
           ElMessage.error("该节点已被添加");
           return;
         }
       }

       if (!"Algorithm1" || !this.selectedLayer || !this.selectedNode) {
         ElMessage.warning("请选择层级和节点");
         return;
       }
       try {
         const response = await axios.post("http://localhost:8889/inspect", {
           algorithm: "Algorithm1",
           layer: this.selectedLayer,
           node: this.selectedNode,
         });
         const chartList = await response.data;
         
         // 先添加所有图表到store
         for (const rawChart of chartList) {
           console.log("add chart", rawChart);
           this.chartStore.addChart(rawChart);
         }
         
         // 等待DOM完全更新后再初始化所有新图表
         await this.$nextTick();
         
         // 使用setTimeout确保DOM完全渲染
         setTimeout(() => {
           // 计算新添加的图表数量
           const newChartsCount = chartList.length;
           const startIndex = this.chartStore.chartOptionsNode.length - newChartsCount;
           
           for (let i = 0; i < newChartsCount; i++) {
             const index = startIndex + i;
             const option = this.chartStore.chartOptionsNode[index];
             const chartRef = this.$refs['chart' + (index + 10000)];
             
             if (chartRef && chartRef[0]) {
               const chartContainer = chartRef[0];
               
               // 确保容器有正确的尺寸
               if (chartContainer.offsetWidth === 0 || chartContainer.offsetHeight === 0) {
                 console.warn(`Chart container has zero size: ${chartContainer.offsetWidth}x${chartContainer.offsetHeight}`);
               }
               
               const chart = echarts.init(chartContainer, null, {renderer: 'svg'});
               
               // 增强图表样式配置
               const enhancedOption = this.enhanceChartOption(option.option);
               chart.setOption(enhancedOption);
               
               // 强制调整图表大小
               setTimeout(() => {
                 chart.resize();
               }, 100);
               
               this.nodeChartList.push({
                 chart: chart,
                 chartTitle: option.chartTitle,
               });
             } else {
               console.error(`Chart ref "node chart${index}" is undefined`);
             }
           }
           
           // 确保所有现有图表也正确调整大小
           this.resizeAllCharts();
           
         }, 50);
         
         ElMessage.success("监控启动成功");
       } catch (error) {
         ElMessage.error("启动监控失败: " + error);
       }
     },
    showFullChart(i, type) {
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
        
        // 构建详细图表配置
        const detailOption = {
          dataZoom: [
              {
                  type: "slider",
                  start: 0,
                  end: 100,
                  backgroundColor: 'rgba(102, 126, 234, 0.1)',
                  dataBackground: {
                    lineStyle: {
                      color: '#667eea'
                    },
                    areaStyle: {
                      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
                        { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
                      ])
                    }
                  },
                  selectedDataBackground: {
                    lineStyle: {
                      color: '#764ba2'
                    },
                    areaStyle: {
                      color: 'rgba(118, 75, 162, 0.3)'
                    }
                  },
                  handleStyle: {
                    color: '#667eea',
                    borderColor: '#667eea'
                  },
                  textStyle: {
                    color: '#667eea'
                  },
                  borderColor: 'rgba(102, 126, 234, 0.2)'
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
            },
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            borderColor: 'rgba(102, 126, 234, 0.2)',
            borderWidth: 1,
            textStyle: {
              color: '#333'
            },
            extraCssText: 'border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);'
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            name: originChart.getOption().xAxis[0].name,
            nameTextStyle: {
              color: '#667eea',
              fontSize: 12,
              fontWeight: 500
            },
            data: this.chartStore.xData[index],
            axisLabel: {
              color: '#666',
              fontSize: 11
            },
            axisLine: {
              lineStyle: {
                color: 'rgba(102, 126, 234, 0.2)'
              }
            },
            splitLine: {
              show: true,
              lineStyle: {
                color: 'rgba(102, 126, 234, 0.1)',
                type: 'dashed'
              }
            }
          },
          toolbox: {
            feature: {
              saveAsImage: {
                backgroundColor: '#fff'
              }
            },
            iconStyle: {
              borderColor: '#667eea'
            },
            emphasis: {
              iconStyle: {
                borderColor: '#764ba2'
              }
            }
          },
          legend: {
            data: originChart.getOption().legend[0].data,
            textStyle: {
              color: '#666',
              fontSize: 12
            },
            itemStyle: {
              borderRadius: 4
            }
          },
          yAxis: {
            type: 'value',
            nameLocation: 'center',
            nameGap: 45,
            name: originChart.getOption().yAxis[0].name,
            nameTextStyle: {
              color: '#667eea',
              fontSize: 12,
              fontWeight: 500
            },
            axisLabel: {
              color: '#666',
              fontSize: 11
            },
            axisLine: {
              lineStyle: {
                color: 'rgba(102, 126, 234, 0.2)'
              }
            },
            splitLine: {
              lineStyle: {
                color: 'rgba(102, 126, 234, 0.1)',
                type: 'dashed'
              }
            }
          },
          series: seriesList,
          grid: {
            left: '6%',
            right: '6%',
            bottom: '18%',
            top: '12%',
            containLabel: true
          }
        };
        
        // 增强详细图表样式配置
        const enhancedDetailOption = this.enhanceChartOption(detailOption);
        chart.setOption(enhancedDetailOption);
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
    
    // 调整所有图表大小的方法
    resizeAllCharts() {
      // 调整总览图表
      this.defaultChartList.forEach(chartItem => {
        if (chartItem.chart && !chartItem.chart.isDisposed()) {
          chartItem.chart.resize();
        }
      });
      
      // 调整节点图表
      this.nodeChartList.forEach(chartItem => {
        if (chartItem.chart && !chartItem.chart.isDisposed()) {
          chartItem.chart.resize();
        }
      });
      
      // 调整用户图表
      this.userChartList.forEach(chartItem => {
        if (chartItem.chart && !chartItem.chart.isDisposed()) {
          chartItem.chart.resize();
        }
      });
    },
    
    // 图表样式增强方法
    enhanceChartOption(originalOption) {
      const enhanced = JSON.parse(JSON.stringify(originalOption));
      
      // 现代化颜色主题
      const colorPalette = [
        '#667eea', '#764ba2', '#f093fb', '#f5576c',
        '#4facfe', '#00f2fe', '#43e97b', '#38f9d7',
        '#667eea', '#764ba2', '#ffecd2', '#fcb69f'
      ];
      
      // 设置全局颜色
      enhanced.color = colorPalette;
      
      // 增强tooltip样式
      enhanced.tooltip = {
        ...enhanced.tooltip,
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: 'rgba(102, 126, 234, 0.2)',
        borderWidth: 1,
        borderRadius: 8,
        textStyle: {
          color: '#333',
          fontSize: 12
        },
        extraCssText: 'box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); backdrop-filter: blur(10px);'
      };
      
      // 增强网格样式
      enhanced.grid = {
        left: '10%',
        right: '5%',
        bottom: '5%',
        top: '20%',
        containLabel: true,
        ...enhanced.grid
      };
      
      // 增强x轴样式
      if (enhanced.xAxis) {
        const xAxis = Array.isArray(enhanced.xAxis) ? enhanced.xAxis[0] : enhanced.xAxis;
        enhanced.xAxis = {
          ...xAxis,
          nameTextStyle: {
            color: '#667eea',
            fontSize: 12,
            fontWeight: 500,
            ...xAxis.nameTextStyle
          },
          axisLabel: {
            color: '#666',
            fontSize: 11,
            ...xAxis.axisLabel
          },
          axisLine: {
            lineStyle: {
              color: 'rgba(102, 126, 234, 0.3)',
              width: 1
            },
            ...xAxis.axisLine
          },
          axisTick: {
            lineStyle: {
              color: 'rgba(102, 126, 234, 0.3)'
            },
            ...xAxis.axisTick
          },
          splitLine: {
            show: true,
            lineStyle: {
              color: 'rgba(102, 126, 234, 0.1)',
              type: 'dashed',
              width: 1
            },
            ...xAxis.splitLine
          }
        };
      }
      
      // 增强y轴样式
      if (enhanced.yAxis) {
        const yAxis = Array.isArray(enhanced.yAxis) ? enhanced.yAxis[0] : enhanced.yAxis;
        enhanced.yAxis = {
          ...yAxis,
          nameTextStyle: {
            color: '#667eea',
            fontSize: 12,
            fontWeight: 500,
            ...yAxis.nameTextStyle
          },
          axisLabel: {
            color: '#666',
            fontSize: 11,
            ...yAxis.axisLabel
          },
          axisLine: {
            lineStyle: {
              color: 'rgba(102, 126, 234, 0.3)',
              width: 1
            },
            ...yAxis.axisLine
          },
          axisTick: {
            lineStyle: {
              color: 'rgba(102, 126, 234, 0.3)'
            },
            ...yAxis.axisTick
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(102, 126, 234, 0.1)',
              type: 'dashed',
              width: 1
            },
            ...yAxis.splitLine
          }
        };
      }
      
      // 增强图例样式
      if (enhanced.legend) {
        enhanced.legend = {
          ...enhanced.legend,
          textStyle: {
            color: '#666',
            fontSize: 12,
            fontWeight: 400
          },
          itemStyle: {
            borderRadius: 4
          },
          itemGap: 20,
          icon: 'roundRect'
        };
      }
      
      // 增强系列样式
      if (enhanced.series) {
        enhanced.series = enhanced.series.map((serie, index) => {
          const enhancedSerie = { ...serie };
          
          if (serie.type === 'line') {
            enhancedSerie.smooth = true;
            enhancedSerie.smoothMonotone = 'x';
            enhancedSerie.symbol = 'circle';
            enhancedSerie.symbolSize = 6;
            enhancedSerie.lineStyle = {
              width: 2,
              cap: 'round',
              join: 'round',
              ...serie.lineStyle
            };
            enhancedSerie.itemStyle = {
              borderWidth: 2,
              borderColor: '#fff',
              shadowBlur: 4,
              shadowColor: 'rgba(0, 0, 0, 0.1)',
              ...serie.itemStyle
            };
            enhancedSerie.emphasis = {
              scale: true,
              scaleSize: 8,
              ...serie.emphasis
            };
            
            // 添加渐变区域填充
            if (!enhancedSerie.areaStyle) {
              const color = colorPalette[index % colorPalette.length];
              enhancedSerie.areaStyle = {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: color + '20' },
                  { offset: 1, color: color + '05' }
                ])
              };
            }
          }
          
          if (serie.type === 'bar') {
            enhancedSerie.itemStyle = {
              borderRadius: [4, 4, 0, 0],
              shadowBlur: 4,
              shadowColor: 'rgba(0, 0, 0, 0.1)',
              shadowOffsetY: 2,
              ...serie.itemStyle
            };
            enhancedSerie.emphasis = {
              itemStyle: {
                shadowBlur: 8,
                shadowColor: 'rgba(0, 0, 0, 0.2)'
              },
              ...serie.emphasis
            };
          }
          
          // 添加动画配置
          enhancedSerie.animationDuration = 1000;
          enhancedSerie.animationEasing = 'cubicOut';
          enhancedSerie.animationDelay = index * 100;
          
          return enhancedSerie;
        });
      }
      
      // 添加全局动画配置
      enhanced.animation = true;
      enhanced.animationDuration = 1000;
      enhanced.animationEasing = 'cubicOut';
      
      return enhanced;
    },
  },
};
</script>

<style scoped>
.charts-container {
  min-height: 100vh;
  padding: var(--spacing-lg);
  background: var(--bg-primary);
}

/* 页面头部样式 */
.page-header {
  margin-bottom: 30px;
}

.header-content {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.title-section {
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0;
  font-weight: 400;
}

/* 图表区域样式 */
.chart-section {
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.header-left {
  flex: 1;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
  font-weight: 400;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.toggle-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  font-weight: 500;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 图表网格样式 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(480px, 1fr));
  gap: 20px;
  margin-top: 20px;
  /* 确保网格在动态添加元素时保持正确的布局 */
  grid-auto-rows: auto;
  grid-auto-flow: row;
}

.chart-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  overflow: hidden;
}

.chart-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.card-header .header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 0;
}

.card-title {
  font-weight: 600;
  font-size: 18px;
  color: var(--text-primary);
}

.view-full-btn {
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 6px;
  font-weight: 500;
}

.chart-container {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.9));
  padding: 8px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

.chart-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.3), transparent);
  z-index: 1;
}

/* 配置选择器样式 */
.detail-content,
.user-content {
  margin-top: 20px;
}

.selector-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.3);
  margin-bottom: 30px;
  overflow: hidden;
}

.card-header .header-right {
  margin-bottom: 0;
}

.status-indicators {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.status-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.status-empty {
  color: var(--text-secondary);
  font-style: italic;
  opacity: 0.6;
}

.selector-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  padding: 20px 0;
}

.selector-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selector-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
  margin-bottom: 8px;
}

.selector-label .el-icon {
  color: var(--primary-color);
  font-size: 16px;
}

.modern-select {
  width: 100%;
}

.add-inspect-btn {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  border: none;
  color: white;
  font-weight: 500;
  padding: 10px 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  align-self: flex-end;
  width: 100%;
  margin-top: auto;
}

.add-inspect-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.add-inspect-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 40px;
  color: var(--text-secondary);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 16px;
  margin-top: 20px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.empty-icon {
  margin-bottom: 20px;
  opacity: 0.6;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  color: var(--primary-color);
  margin: 0 auto 20px;
}

.empty-state h3 {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--text-primary);
  font-weight: 600;
}

.empty-state p {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 20px;
}

.quick-setup-btn,
.create-chart-btn {
  font-weight: 500;
  padding: 10px 20px;
  border-radius: 8px;
}

/* 收起状态样式 */
.collapsed-info {
  text-align: center;
  padding: 40px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 16px;
  margin-top: 20px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.collapsed-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  color: var(--primary-color);
  margin: 0 auto 16px;
}

/* 对话框样式 */
.chart-dialog {
  border-radius: 16px;
}

.chart-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 20px;
  border-radius: 16px 16px 0 0;
}

.chart-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.dialog-chart-container {
  width: 100%;
  height: 500px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 252, 0.95));
  padding: 12px;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

.dialog-chart-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.5), transparent);
  z-index: 1;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.export-btn {
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .charts-container {
    padding: 15px;
  }
  
  .chart-section {
    padding: 20px;
  }
  
  .header-content {
    padding: 20px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
    grid-auto-rows: auto;
  }
  
  .selector-row {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .status-indicators {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .chart-dialog {
    width: 95% !important;
    max-width: 90vw !important;
  }
  
  .dialog-chart-container {
    height: 350px;
    padding: 8px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 15px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .dialog-chart-container {
    height: 350px;
  }
}

/* Element Plus 组件覆盖样式 */
.modern-select :deep(.el-input__wrapper) {
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
}

.modern-select :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  border-color: var(--primary-color);
}

.chart-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

/* 图表容器额外样式 */
.chart-container canvas,
.dialog-chart-container canvas {
  border-radius: 6px;
}

/* 图表hover效果 */
.chart-card:hover .chart-container::before {
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.5), transparent);
  animation: shimmer 2s ease-in-out infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 图表加载状态 */
.chart-container.loading {
  position: relative;
}

.chart-container.loading::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 40px;
  height: 40px;
  margin: -20px 0 0 -20px;
  border: 3px solid rgba(102, 126, 234, 0.1);
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  z-index: 10;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 图表数据点动画 */
.chart-container:hover {
  transform: scale(1.01);
  transition: transform 0.3s ease;
}

/* 美化滚动条 */
.charts-grid::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.charts-grid::-webkit-scrollbar-track {
  background: rgba(102, 126, 234, 0.1);
  border-radius: 3px;
}

.charts-grid::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 3px;
}

.charts-grid::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2, #667eea);
}


</style>